package com.xiaojiesir.demo.argumentResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.springframework.annotation.MyRequestParam;
import com.springframework.annotation.MyService;

@MyService("requestParamArgumentResolver")
public class RequestParamArgumentResolver implements ArgumentResolver {
 
	@Override
	public boolean support(Class<?> type, int paramIndex, Method method) {
		// type = class java.lang.String
		// @MyRequestParam("name")String name
		//获取当前方法的参数
		Annotation[][] an = method.getParameterAnnotations();
		Annotation[] paramAns = an[paramIndex];
		
		for (Annotation paramAn : paramAns) {
        	//判断传进的paramAn.getClass()是不是 MyRequestParam 类型
        	if (MyRequestParam.class.isAssignableFrom(paramAn.getClass())) {
                return true;
            }
        }
		
		return false;
	}
 
	@Override
	public Object argumentResolver(HttpServletRequest request,
			HttpServletResponse response, Class<?> type, int paramIndex,
			Method method) {
		
		//获取当前方法的参数
		Annotation[][] an = method.getParameterAnnotations();
		Annotation[] paramAns = an[paramIndex];
		
		for (Annotation paramAn : paramAns) {
        	//判断传进的paramAn.getClass()是不是 MyRequestParam 类型
        	if (MyRequestParam.class.isAssignableFrom(paramAn.getClass())) {
        		MyRequestParam cr = (MyRequestParam) paramAn;
                String value = cr.value();
                return request.getParameter(value);
            }
        }
		return null;
	}
 
}
