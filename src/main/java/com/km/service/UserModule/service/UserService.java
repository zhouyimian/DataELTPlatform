package com.km.service.UserModule.service;

import com.km.service.UserModule.Mapper.UserMapper;
import com.km.service.UserModule.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserMapper userMapper;

    public User login(String username,String password){
        return userMapper.login(username,password);
    }

    public void register(String userid,String username,String password){
        userMapper.register(userid,username,password,"");
    }

    public User findUserByUserName(String username) {
        return userMapper.findUserByUserName(username);
    }
}
