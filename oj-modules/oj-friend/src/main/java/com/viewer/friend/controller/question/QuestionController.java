package com.viewer.friend.controller.question;

import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.Service.question.IQuestionService;
import com.viewer.friend.domain.question.dto.QuestionQueryDTO;
import com.viewer.friend.domain.question.vo.QuestionDetailVO;
import com.viewer.friend.domain.question.vo.QuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {

    @Autowired
    private IQuestionService questionService;

    @GetMapping("/semiLogin/list")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        return questionService.list(questionQueryDTO);
    }

    @GetMapping("/semiLogin/dbList")
    public TableDataInfo dbList(QuestionQueryDTO questionQueryDTO) {
        //数据库版题目列表接口
        return null;
    }

    @GetMapping("/semiLogin/hotList")
    public Result<List<QuestionVO>> hotList() {
        return Result.success(questionService.hotList());
    }

    @GetMapping("/detail")
    public Result<QuestionDetailVO> detail(Long questionId) {
        return Result.success(questionService.detail(questionId));
    }

    //题目的顺序列表 : 先从redis  redis中没有数据查询数据库
    // 当前题目是哪个（questionId）
    //redis  list  数据类型  key: q:l value : questionId
    @GetMapping("/preQuestion")
    public Result<String> preQuestion(Long questionId) {
        return Result.success(questionService.preQuestion(questionId));
    }

    @GetMapping("/nextQuestion")
    public Result<String> nextQuestion(Long questionId) {
        return Result.success(questionService.nextQuestion(questionId));
    }
}
