package com.iot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by xiongxiaoyu
 * Data:2018/6/23
 * Time:21:12
 */


//不理解
//此注解表示这是一个远程接口
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
	//value代表提供服务的接口
	Class<?> value();
}