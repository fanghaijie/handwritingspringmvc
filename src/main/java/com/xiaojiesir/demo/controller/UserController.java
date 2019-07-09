package com.xiaojiesir.demo.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.springframework.annotation.MyAutowired;
import com.springframework.annotation.MyController;
import com.springframework.annotation.MyRequestMapping;
import com.springframework.annotation.MyRequestParam;
import com.xiaojiesir.demo.pojo.User;
import com.xiaojiesir.demo.service.UserService;

@MyController
@MyRequestMapping("/demo")
public class UserController {

	@MyAutowired
	private UserService userService;
	
	@MyRequestMapping("/user")
	public void getUser(HttpServletRequest req,HttpServletResponse rep,@MyRequestParam("name") String name,@MyRequestParam("id") String id){
		User u = userService.getUser();
		System.out.println(u);
		System.out.println(name);
		System.out.println(id);
		try {
			rep.getWriter().write("name:"+name+";id:"+id);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
