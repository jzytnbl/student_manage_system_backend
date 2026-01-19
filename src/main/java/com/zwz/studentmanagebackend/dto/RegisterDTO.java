package com.zwz.studentmanagebackend.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 20, message = "用户名长度必须在3到20个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;

    // 邮箱可选，但如果填写了必须符合格式
    @Email(message = "邮箱格式不正确（例如：user@example.com）")
    private String email;

    // 手机号可选，但如果填写了必须符合格式
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确（必须是11位，1开头）")
    private String phone;

    @NotBlank(message = "角色不能为空")
    private String role; // 角色：STUDENT, TEACHER, ADMIN
}
