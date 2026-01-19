package com.zwz.studentmanagebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        // 收集所有字段的错误信息
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // 构建友好的错误消息
        StringBuilder message = new StringBuilder("输入验证失败：");
        errors.forEach((field, error) -> {
            message.append("\n• ").append(getFieldName(field)).append("：").append(error);
        });
        
        response.put("message", message.toString());
        response.put("errors", errors);
        response.put("status", 400);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        
        String message = ex.getMessage();
        
        // 判断是否是用户名已存在的错误
        if (message != null && message.contains("用户名已存在")) {
            response.put("message", "该用户名已被注册，请换一个用户名");
            response.put("status", 409);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        response.put("message", message != null ? message : "操作失败，请重试");
        response.put("status", 500);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "服务器内部错误：" + ex.getMessage());
        response.put("status", 500);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 将字段名转换为中文
     */
    private String getFieldName(String field) {
        switch (field) {
            case "username":
                return "用户名";
            case "password":
                return "密码";
            case "email":
                return "邮箱";
            case "phone":
                return "手机号";
            case "role":
                return "角色";
            default:
                return field;
        }
    }
}
