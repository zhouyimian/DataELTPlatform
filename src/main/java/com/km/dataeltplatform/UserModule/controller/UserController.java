package com.km.dataeltplatform.UserModule.controller;

import com.km.dataeltplatform.UserModule.domain.User;
import com.km.dataeltplatform.UserModule.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpServletRequest req) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        User user = userService.login(username,password);
        return user.toString();
    }
}
