package com.zwz.studentmanagebackend.controller;

import com.zwz.studentmanagebackend.dto.LoginDTO;
import com.zwz.studentmanagebackend.dto.RegisterDTO;
import com.zwz.studentmanagebackend.dto.UserDTO;
import com.zwz.studentmanagebackend.entity.User;
import com.zwz.studentmanagebackend.security.JwtUtil;
import com.zwz.studentmanagebackend.service.AuthService;
import com.zwz.studentmanagebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(registerDTO.getUsername());
        userDTO.setPassword(registerDTO.getPassword());
        // 使用前端传来的角色，如果没有则默认为STUDENT
        userDTO.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : "STUDENT");
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> profile(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "未授权");
                return ResponseEntity.status(401).body(error);
            }
            
            String token = authHeader.substring(7);
            
            // 从token中提取用户名
            String username = jwtUtil.extractUsername(token);
            if (username == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "Token无效");
                return ResponseEntity.status(401).body(error);
            }
            
            // 查询用户信息
            User user = userService.findByUsername(username);
            if (user == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "用户不存在");
                return ResponseEntity.status(404).body(error);
            }
            
            // 构建返回数据
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("createdAt", user.getCreatedAt());
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "获取用户信息失败：" + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}