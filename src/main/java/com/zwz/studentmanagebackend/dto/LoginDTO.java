package com.zwz.studentmanagebackend.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String username;
    private String password;
    private Boolean rememberMe = false;
}