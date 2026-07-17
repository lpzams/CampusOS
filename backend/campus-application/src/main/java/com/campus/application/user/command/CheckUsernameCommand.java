package com.campus.application.user.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckUsernameCommand {
    @NotBlank(message = "用户名不能为空")
    private String username;
}