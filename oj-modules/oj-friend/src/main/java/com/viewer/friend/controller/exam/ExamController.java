package com.viewer.friend.controller.exam;


import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.friend.domain.exam.dto.ExamQueryDTO;
import com.viewer.friend.Service.exam.impl.ExamServiceImpl;
import com.viewer.friend.domain.exam.dto.ExamRankDTO;
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

    @GetMapping("/list")
    public TableDataInfo getList(ExamQueryDTO examQueryDTO){
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    @GetMapping("/semiLogin/redis/list")
    public TableDataInfo redisGetList(ExamQueryDTO examQueryDTO){

        return examService.redisGetList(examQueryDTO);
    }

    @GetMapping("/rank/list")
    public TableDataInfo rankList(ExamRankDTO examRankDTO) {
        return examService.rankList(examRankDTO);
    }

    @GetMapping("/getFirstQuestion")
    public Result<?> getFirstQuestion(){
        return new Result<>();
    }

    @GetMapping("/preQuestion")
    public Result<?> preQuestion(){
        return new Result<>();
    }

    @GetMapping("/nextQuestion")
    public Result<?> nextQuestion(){
        return new Result<>();
    }

}
