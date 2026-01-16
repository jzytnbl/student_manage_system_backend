package com.zwz.studentmanagebackend.aop;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    int value() default 100;
    int time() default 60;
    String message() default "请求过于频繁";
}