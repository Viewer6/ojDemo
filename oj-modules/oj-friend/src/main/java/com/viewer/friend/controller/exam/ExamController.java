package com.viewer.friend.controller.exam;


import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import com.viewer.friend.Service.exam.impl.ExamServiceImpl;
import com.viewer.friend.mapper.exam.ExamMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {

    @Resource(name = "examServiceImpl")
    private ExamServiceImpl examService;

    @GetMapping("/semiLogin/list")
    public TableDataInfo getList(ExamQueryDTO examQueryDTO){
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    @GetMapping("/redis/list")
    public TableDataInfo redisGetList(@RequestBody ExamQueryDTO examQueryDTO){
        return examService.redisGetList(examQueryDTO);
    }
}
