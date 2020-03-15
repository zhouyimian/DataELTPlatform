package com.km.service.UserModule.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.km.service.UserModule.domain.User;
import com.km.service.UserModule.service.UserService;
import com.km.service.common.UnAuthToken;
import com.km.service.common.exception.serviceException;
import com.km.service.common.utils.MD5Utils;
import com.km.service.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sun.security.provider.MD5;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @UnAuthToken
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Object login(HttpServletRequest req) {
        String username = req.getParameter("username");
        String password = MD5Utils.getMD5(req.getParameter("password"));
        User user = userService.login(username,password);
        if(user==null) {
            return new serviceException("用户名或者密码错误");
        }
        String source = JSONObject.toJSONString(user)+System.currentTimeMillis();
        String token = MD5Utils.getMD5(source);
        redisUtil.set(token,JSONObject.toJSONString(user),60*60, TimeUnit.SECONDS);

        JSONObject message = new JSONObject();
        message.put("token",token);
        message.put("nickname",null==user.getNickName()?username:user.getNickName());
        return JSONObject.toJSON(message);
    }

    @UnAuthToken
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Object register(HttpServletRequest req){
        String username = req.getParameter("username");
        String password = MD5Utils.getMD5(req.getParameter("password"));
        String userid = UUID.randomUUID().toString().replace("-","");
        User user = userService.findUserByUserName(username);
        if(user!=null){
            throw new serviceException("该用户名已被注册");
        }else{
            userService.register(userid,username,password);
        }
        JSONObject message = new JSONObject();
        message.put("message","注册成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public Object logout(HttpServletRequest req){
        String token = req.getHeader("token");
        redisUtil.delete(token);
        JSONObject message = new JSONObject();
        message.put("message","注销成功");
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public String test(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        return req.getParameter("username");
    }
}
