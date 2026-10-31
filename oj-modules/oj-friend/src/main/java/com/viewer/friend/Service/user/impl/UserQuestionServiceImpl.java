package com.viewer.friend.Service.user.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;

import com.viewer.common.core.constants.Constants;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.enums.ProgramType;
import com.viewer.common.core.enums.QuestionResType;
import com.viewer.common.core.enums.ResultCode;
import com.viewer.common.core.exception.ServiceException;
import com.viewer.common.core.utils.ThreadLocalUtil;
import com.viewer.friend.Service.user.IUserQuestionService;
import com.viewer.friend.domain.question.Question;
import com.viewer.friend.domain.question.QuestionCase;
import com.viewer.friend.domain.question.es.QuestionES;
import com.viewer.friend.domain.user.UserSubmit;
import com.viewer.friend.domain.user.dot.UserSubmitDTO;
import com.viewer.friend.elasticsearch.QuestionRepository;
import com.viewer.friend.mapper.question.QuestionMapper;
import com.viewer.friend.mapper.user.UserSubmitMapper;
import com.viewer.friend.rabbit.JudgeProducer;
import lombok.extern.slf4j.Slf4j;
import org.example.api.RemoteJudgeService;
import org.example.api.domain.UserExeResult;
import org.example.api.domain.dto.JudgeSubmitDTO;
import org.example.api.domain.vo.UserQuestionResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserQuestionServiceImpl implements IUserQuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    @Autowired
    private RemoteJudgeService remoteJudgeService;

    @Autowired
    private JudgeProducer judgeProducer;

    @Override
    public Result<UserQuestionResultVO> submit(UserSubmitDTO submitDTO) {
        //todo: 添加其他类型判题
        Integer programType = submitDTO.getProgramType();
        if (ProgramType.JAVA.getValue().equals(programType)) {
            //按照java逻辑处理
            JudgeSubmitDTO judgeSubmitDTO = assembleJudgeSubmitDTO(submitDTO);
            return remoteJudgeService.doJudgeJavaCode(judgeSubmitDTO);
        }
        throw new ServiceException(ResultCode.FAILED_NOT_SUPPORT_PROGRAM);
    }

    @Override
    public boolean rabbitSubmit(UserSubmitDTO submitDTO) {
        Integer programType = submitDTO.getProgramType();
        if (ProgramType.JAVA.getValue().equals(programType)) {
            //按照java逻辑处理
            JudgeSubmitDTO judgeSubmitDTO = assembleJudgeSubmitDTO(submitDTO);
            judgeProducer.produceMsg(judgeSubmitDTO);
            return true;
        }
        throw new ServiceException(ResultCode.FAILED_NOT_SUPPORT_PROGRAM);
    }

    @Override
    public UserQuestionResultVO exeResult(Long examId, Long questionId, String currentTime) {
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        UserSubmit userSubmit = userSubmitMapper.selectCurrentUserSubmit(userId, examId, questionId, currentTime);
        UserQuestionResultVO resultVO = new UserQuestionResultVO();
        if (userSubmit == null) {
            resultVO.setPass(QuestionResType.IN_JUDGE.getValue());
        } else {
            resultVO.setPass(userSubmit.getPass());
            resultVO.setExeMessage(userSubmit.getExeMessage());
            if (StrUtil.isNotEmpty(userSubmit.getCaseJudgeRes())) {
                resultVO.setUserExeResultList(JSON.parseArray(userSubmit.getCaseJudgeRes(), UserExeResult.class));
            }
        }
        return resultVO;
    }

    /**
     * 组装判题参数。
     * <p>
     * 题目用例 questionCase 必须保存为 [{"input":"...","output":"..."}] 形式的 JSON 数组，
     * 否则这里无法得到每个用例的输入/输出。ES 中的题目文档只在首次提交时从 MySQL 同步，
     * 因此题库在 B 端被修改后 ES 里可能仍是旧数据，解析失败时需要回源 MySQL 重新加载。
     */
    private JudgeSubmitDTO assembleJudgeSubmitDTO(UserSubmitDTO submitDTO) {
        Long questionId = submitDTO.getQuestionId();
        if (questionId == null) {
            throw new ServiceException(ResultCode.FAILED_PARAMS_VALIDATE, "题目id不能为空");
        }

        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
        boolean refreshEs = questionES == null;
        if (refreshEs) {
            //ES中没有该题目，回源MySQL
            questionES = loadQuestionFromDb(questionId);
        }

        List<QuestionCase> questionCaseList = parseQuestionCase(questionES.getQuestionCase());
        if (questionCaseList == null && !refreshEs) {
            //ES中是旧数据（B端已修改但未同步ES），以MySQL为准重新加载
            log.warn("题目[{}]在ES中的用例数据不是合法的JSON数组，回源MySQL重新加载", questionId);
            questionES = loadQuestionFromDb(questionId);
            questionCaseList = parseQuestionCase(questionES.getQuestionCase());
            refreshEs = true;
        }
        if (questionCaseList == null) {
            log.error("题目[{}]用例配置错误, questionCase={}", questionId, questionES.getQuestionCase());
            throw new ServiceException(ResultCode.FAILED_QUESTION_CASE_INVALID,
                    "题目用例配置有误（questionId=" + questionId
                            + "），题目用例必须是形如 [{\"input\":\"1 2\",\"output\":\"3\"}] 的JSON数组");
        }
        if (refreshEs) {
            //用例校验通过后再刷新ES，避免把非法数据写进索引
            questionRepository.save(questionES);
        }

        JudgeSubmitDTO judgeSubmitDTO = new JudgeSubmitDTO();
        BeanUtil.copyProperties(questionES, judgeSubmitDTO);
        judgeSubmitDTO.setUserId(ThreadLocalUtil.get(Constants.USER_ID, Long.class));
        judgeSubmitDTO.setExamId(submitDTO.getExamId());
        judgeSubmitDTO.setProgramType(submitDTO.getProgramType());
        judgeSubmitDTO.setUserCode(codeConnect(submitDTO.getUserCode(), questionES.getMainFuc()));
        List<String> inputList = questionCaseList.stream().map(QuestionCase::getInput).toList();
        judgeSubmitDTO.setInputList(inputList);
        List<String> outputList = questionCaseList.stream().map(QuestionCase::getOutput).toList();
        judgeSubmitDTO.setOutputList(outputList);
        return judgeSubmitDTO;
    }

    private QuestionES loadQuestionFromDb(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new ServiceException(ResultCode.FAILED_QUESTION_NOT_EXISTS);
        }
        QuestionES questionES = new QuestionES();
        BeanUtil.copyProperties(question, questionES);
        return questionES;
    }

    /**
     * 解析题目用例，格式不合法（或没有用例）时返回null。
     */
    private List<QuestionCase> parseQuestionCase(String questionCase) {
        if (StrUtil.isBlank(questionCase)) {
            return null;
        }
        List<QuestionCase> questionCaseList;
        try {
            questionCaseList = JSONUtil.toList(questionCase, QuestionCase.class);
        } catch (Exception e) {
            log.warn("题目用例解析失败: {}", questionCase, e);
            return null;
        }
        if (questionCaseList == null || questionCaseList.isEmpty()) {
            return null;
        }
        boolean incomplete = questionCaseList.stream()
                .anyMatch(item -> item == null || item.getInput() == null || item.getOutput() == null);
        if (incomplete) {
            log.warn("题目用例缺少input/output字段: {}", questionCase);
            return null;
        }
        return questionCaseList;
    }

    private String codeConnect(String userCode, String mainFunc) {
        if (StrUtil.isBlank(userCode)) {
            throw new ServiceException(ResultCode.FAILED_PARAMS_VALIDATE, "用户代码不能为空");
        }
        if (StrUtil.isBlank(mainFunc)) {
            throw new ServiceException(ResultCode.ERROR, "题目未配置 main 函数模板");
        }
        String targetCharacter = "}";
        int targetLastIndex = userCode.lastIndexOf(targetCharacter);
        if (targetLastIndex != -1) {
            return userCode.substring(0, targetLastIndex) + "\n" + mainFunc + "\n" + userCode.substring(targetLastIndex);
        }
        throw new ServiceException(ResultCode.FAILED_PARAMS_VALIDATE, "用户代码缺少右花括号 }，请检查提交内容");
    }
}
