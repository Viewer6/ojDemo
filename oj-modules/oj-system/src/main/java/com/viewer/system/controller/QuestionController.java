package com.viewer.system.controller;

import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.system.domain.question.dto.QuestionListDTO;
import com.viewer.system.domain.question.vo.QuestionListVO;
import com.viewer.system.service.question.impl.QuestionServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "题目管理")
@Slf4j
@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {

    @Resource(name = "questionServiceImpl")
    private QuestionServiceImpl questionService;

    @GetMapping("/getList")
    public TableDataInfo getList(QuestionListDTO questionListDTO){
        return getTableDataInfo(questionService.getList(questionListDTO));
    }
}
