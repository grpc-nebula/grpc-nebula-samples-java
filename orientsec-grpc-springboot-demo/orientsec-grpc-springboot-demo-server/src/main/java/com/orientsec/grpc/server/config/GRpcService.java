package com.orientsec.grpc.server.config;

import io.grpc.ServerInterceptor;
import org.springframework.stereotype.Service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于获取Spring扫描到的类
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface GRpcService {

    boolean applyGlobalInterceptors() default true;

    Class<? extends ServerInterceptor>[] interceptors() default {};
}