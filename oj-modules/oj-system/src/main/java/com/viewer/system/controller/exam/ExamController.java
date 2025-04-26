package com.viewer.system.controller.exam;

import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.system.domain.exam.dto.ExamQueryDTO;
import com.viewer.system.service.exam.impl.ExamServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
