package com.km.dataeltplatform.UserModule.controller;

import com.km.dataeltplatform.UserModule.domain.User;
import com.km.dataeltplatform.UserModule.service.UserService;
import com.km.dataeltplatform.common.myException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpServletRequest req) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        User user = userService.login(username,password);
        if(user==null)
            return new myException("用户名或者密码错误").toString();
        return user.toString();
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(HttpServletRequest req) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String userid = UUID.randomUUID().toString().replace("-","");
        User user = userService.login(username,password);
        if(user!=null){
            return new myException("该用户已被注册").toString();
        }else{
            userService.register(userid,username,password);
            user = userService.login(username,password);
        }
        return user.toString();
    }
}
