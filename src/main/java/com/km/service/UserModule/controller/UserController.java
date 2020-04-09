package com.km.service.UserModule.controller;

import com.alibaba.fastjson.JSONObject;
import com.km.service.ConfigureModule.dto.ConfUseridDto;
import com.km.service.ConfigureModule.service.ConfigureService;
import com.km.service.DeploymentModule.dto.DeploymentUseridDto;
import com.km.service.DeploymentModule.service.DeploymentService;
import com.km.service.ProcessModule.dto.ProcessUseridDto;
import com.km.service.ProcessModule.service.ProcessService;
import com.km.service.UserModule.domain.User;
import com.km.service.UserModule.service.UserService;
import com.km.service.common.UnAuthToken;
import com.km.service.common.exception.serviceException;
import com.km.service.common.utils.MD5Utils;
import com.km.service.common.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ConfigureService configureService;

    @Autowired
    private DeploymentService deploymentService;

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
        message.put("nickname", StringUtils.isBlank(user.getNickName())?username:user.getNickName());
        return JSONObject.toJSON(message);
    }

    @UnAuthToken
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Object register(HttpServletRequest req){
        String username = req.getParameter("username");
        String password = MD5Utils.getMD5(req.getParameter("password"));
        String userid = UUID.randomUUID().toString().replace("-","");
        User user = userService.getUserByUserName(username);
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


    @RequestMapping(value = "/getAllUserInformation", method = RequestMethod.POST)
    public Object getAllUserInformation(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        JSONObject message = new JSONObject();
        List<ProcessUseridDto> processDesc = processService.getAllPrivateProcess(user.getUserId());



        List<ConfUseridDto> confDesc = configureService.getAllPrivateConfigures(user.getUserId());
        List<DeploymentUseridDto> privateDeployDesc = deploymentService.getAllPrivateDeployments(user.getUserId());
        List<DeploymentUseridDto> permissionDeployDesc = deploymentService.getAllPermissionDeployments(user.getUserId());

        message.put("processDesc",processDesc);
        message.put("confDesc",confDesc);
        message.put("deployDesc",privateDeployDesc);
        message.put("permissionDeployDesc",permissionDeployDesc);
        return JSONObject.toJSON(message);
    }
}
