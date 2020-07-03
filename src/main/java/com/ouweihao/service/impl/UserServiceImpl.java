package com.ouweihao.service.impl;

import com.ouweihao.dao.UserDao;
import com.ouweihao.pojo.User;
import com.ouweihao.service.UserService;
import com.ouweihao.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public User checkUser(String username, String password) {
        User user = userDao.queryByUsernameAndPassword(username, MD5Utils.code(password));
        return user;
    }
}
