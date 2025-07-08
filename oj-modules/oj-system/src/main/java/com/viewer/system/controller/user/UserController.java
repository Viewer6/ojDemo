package com.viewer.system.controller.user;


import com.viewer.common.core.controller.BaseController;
import com.viewer.common.core.domain.Result;
import com.viewer.common.core.domain.TableDataInfo;
import com.viewer.system.domain.user.dto.UserDTO;
import com.viewer.system.domain.user.dto.UserQueryDTO;
import com.viewer.system.service.user.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Resource(name = "userServiceImpl")
    private IUserService userService;

    @GetMapping("/list")
    public TableDataInfo list(UserQueryDTO userQueryDTO) {
        return getTableDataInfo(userService.list(userQueryDTO));
    }

    @PutMapping("/updateStatus")
    //todo 拉黑：限制用户操作   解禁：放开对于用户限制
    //更新数据库中用户的状态信息。
    public Result<Void> updateStatus(@RequestBody UserDTO userDTO) {
        return getResult(userService.updateStatus(userDTO));
    }
}
