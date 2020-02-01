package com.km.service.PermissionModule.controller;

import com.alibaba.fastjson.JSONObject;
import com.km.service.PermissionModule.service.PermissionService;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.dto.ProcessUseridDto;
import com.km.service.ProcessModule.service.ProcessService;
import com.km.service.UserModule.domain.User;
import com.km.service.UserModule.service.UserService;
import com.km.service.common.exception.serviceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/authorizeProcess", method = RequestMethod.POST)
    public Object authorizeProcess(HttpServletRequest req) {
        String processId = req.getParameter("processId");
        String username = req.getParameter("username");
        User user = userService.findUserByUserName(username);
        User nowUser = (User) req.getAttribute("user");
        Process process = processService.getProcessByProcessId(processId);
        if(user==null){
            throw new serviceException("该用户名不存在");
        }
        if(nowUser.getUserId()!=process.getUserId()){
            throw new serviceException("当前用户并不是该流程的创建者,无法进行授权");
        }
        permissionService.authorize(user.getUserId(),processId);
        JSONObject message = new JSONObject();
        message.put("message","授权成功");
        return JSONObject.toJSON(message);
    }


}
