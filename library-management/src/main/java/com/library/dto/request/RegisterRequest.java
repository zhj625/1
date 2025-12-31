package com.library.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度6-100个字符")
    private String password;

    @Size(max = 50, message = "真实姓名不超过50个字符")
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号不超过20个字符")
    private String phone;
}
