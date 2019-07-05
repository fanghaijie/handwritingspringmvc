package com.springframework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 
* 
* @ClassName: MyAutowired.java
* @Description: 该类的功能描述
*
* @version: v1.0.0
* @author: Lenovo
* @date: 2017年12月12日 下午8:46:44 
*
* Modification History:
* Date         Author          Version            Description
*---------------------------------------------------------*
* 2017年12月12日     Lenovo           v1.0.0               修改原因
 */
@Retention(RetentionPolicy.RUNTIME)//注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
@Target(ElementType.FIELD)//字段、枚举的常量
public @interface MyAutowired {

	String value() default "";
}
