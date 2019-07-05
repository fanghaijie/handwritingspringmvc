package com.xiaojiesir.demo.service.impl;

import com.springframework.annotation.MyAutowired;
import com.springframework.annotation.MyService;
import com.xiaojiesir.demo.dao.UserDao;
import com.xiaojiesir.demo.pojo.User;
import com.xiaojiesir.demo.service.UserService;

@MyService
public class UserServiceImpl implements UserService {
	@MyAutowired
	private UserDao userdao;

	@Override
	public User getUser() {
		// TODO Auto-generated method stub
		return userdao.getUser();
	}

}
