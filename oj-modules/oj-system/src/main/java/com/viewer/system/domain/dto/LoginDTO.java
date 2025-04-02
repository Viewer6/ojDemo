package com.viewer.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginDTO {
    @NotNull(message = "用户名不能为空")
    @Schema(description = "用户账号")
    private String userAccount;
    @NotNull(message = "密码不能为空")
    @Schema(description = "用户密码")
    private String password;
}
