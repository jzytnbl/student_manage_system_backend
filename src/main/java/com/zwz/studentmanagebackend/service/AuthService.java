package com.zwz.studentmanagebackend.service;

import com.zwz.studentmanagebackend.dto.LoginDTO;
import com.zwz.studentmanagebackend.entity.User;
import com.zwz.studentmanagebackend.repository.UserRepository;
import com.zwz.studentmanagebackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public Map<String, Object> login(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );

            String token = jwtUtil.generateToken(loginDTO.getUsername());

            // Redis存储token
            String redisKey = "token:" + loginDTO.getUsername();
            redisTemplate.opsForValue().set(redisKey, token, 2, TimeUnit.HOURS);

            User user = userRepository.findByUsername(loginDTO.getUsername()).orElse(null);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("username", user.getUsername());
            result.put("role", user.getRole());
            result.put("userId", user.getId());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    public void logout(String token) {
        String username = jwtUtil.extractUsername(token);
        String redisKey = "token:" + username;
        redisTemplate.delete(redisKey);
    }

    public Boolean validateToken(String token) {
        String username = jwtUtil.extractUsername(token);
        String redisKey = "token:" + username;
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        return storedToken != null && storedToken.equals(token) && !jwtUtil.isTokenExpired(token);
    }
}