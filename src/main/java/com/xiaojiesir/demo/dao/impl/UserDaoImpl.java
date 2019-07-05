package com.xiaojiesir.demo.dao.impl;

import com.springframework.annotation.MyRepository;
import com.xiaojiesir.demo.dao.UserDao;
import com.xiaojiesir.demo.pojo.User;

@MyRepository
public class UserDaoImpl implements UserDao {

	@Override
	public User getUser() {
		// TODO Auto-generated method stub
		User u = new User();
		u.setId(1);
		u.setName("xiaojiesir");
		return u;
	}

}
