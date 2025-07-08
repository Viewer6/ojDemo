package com.viewer.system.controller.exam;

import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.system.domain.exam.dto.ExamAddDTO;
import com.viewer.system.domain.exam.dto.ExamEditDTO;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.domain.exam.dto.ExamQuestionAddDTO;
import com.viewer.system.domain.exam.vo.ExamDetailVO;
import com.viewer.system.service.exam.impl.ExamServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "竞赛管理")
@Slf4j
@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {

    @Resource(name = "examServiceImpl")
    private ExamServiceImpl examService;

    @GetMapping("/getList")
    public TableDataInfo getList(ExamQueryDTO examQueryDTO){
        return getTableDataInfo(examService.getList(examQueryDTO));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody ExamAddDTO examAddDTO){
        return Result.success(examService.add(examAddDTO));
    }

    @PostMapping("/question/add")
    public Result<Void> addQuestion(@RequestBody ExamQuestionAddDTO questionAddDTO){
        return getResult(examService.addQuestion(questionAddDTO));
    }

    @DeleteMapping("/question/delete")
    public Result<Void> deleteQuestion(Long examId, Long questionId){
        return getResult(examService.deleteQuestion(examId, questionId));
    }

    @GetMapping("/detail")
    public Result<ExamDetailVO> detail(Long examId){
        return Result.success(examService.detail(examId));
    }

    @PutMapping("/edit")
    public Result<Void> edit(@RequestBody ExamEditDTO examEditDTO){
        return getResult(examService.edit(examEditDTO));
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(Long examId){
        return getResult(examService.delete(examId));
    }

    @PutMapping("/publish")
    public Result<Void> publish(Long examId){
        return getResult(examService.publish(examId));
    }

    @PutMapping("/cancelPublish")
    public Result<Void> cancelPublish(Long examId){
        return getResult(examService.cancelPublish(examId));
    }
}
