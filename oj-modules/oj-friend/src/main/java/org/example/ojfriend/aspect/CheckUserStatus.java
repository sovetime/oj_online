package org.example.ojfriend.aspect;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})//该注解可以应用于类或方法
@Retention(RetentionPolicy.RUNTIME) //该注解会在运行时保留，可以通过反射获取
@Documented //该注解会被包含在JavaDoc中
public @interface CheckUserStatus {

}
