package com.zwz.studentmanagebackend.controller;

import com.zwz.studentmanagebackend.dto.LoginDTO;
import com.zwz.studentmanagebackend.dto.RegisterDTO;
import com.zwz.studentmanagebackend.dto.UserDTO;
import com.zwz.studentmanagebackend.service.AuthService;
import com.zwz.studentmanagebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(registerDTO.getUsername());
        userDTO.setPassword(registerDTO.getPassword());
        userDTO.setRole("ADMIN"); // 默认注册为管理员，可以根据需求调整
        userDTO.setEmail(registerDTO.getEmail());
        userDTO.setPhone(registerDTO.getPhone());

        userService.createUser(userDTO);

        // 注册后自动登录
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(registerDTO.getUsername());
        loginDTO.setPassword(registerDTO.getPassword());

        return authService.login(loginDTO);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        authService.logout(token);
        return "登出成功";
    }

    @GetMapping("/profile")
    public String profile() {
        return "用户信息";
    }
}