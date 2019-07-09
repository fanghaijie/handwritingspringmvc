package com.xiaojiesir.demo.argumentResolver;

import java.lang.reflect.Method;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.springframework.annotation.MyService;

/*
 * 处理Request请求参数
 */
@MyService("httpServletRequestArgumentResolver")
public class HttpServletRequestArgumentResolver implements ArgumentResolver {
	 
	@Override
	public boolean support(Class<?> type, int paramIndex, Method method) {
		return ServletRequest.class.isAssignableFrom(type);
	}
 
	@Override
	public Object argumentResolver(HttpServletRequest request,
			HttpServletResponse response, Class<?> type, int paramIndex,
			Method method) {
		return request;
	}
 
}
