package com.km.service.DeploymentModule.controller;

import com.alibaba.fastjson.JSONObject;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ConfigureModule.service.ConfigureService;
import com.km.service.DeploymentModule.domain.Deployment;
import com.km.service.DeploymentModule.dto.DeploymentUseridDto;
import com.km.service.DeploymentModule.service.DeploymentService;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.service.ProcessService;
import com.km.service.UserModule.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class DeploymentController {

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ConfigureService configureService;

    @Autowired
    private ProcessService processService;


    @RequestMapping(value = "/getAllDeployments", method = RequestMethod.POST)
    public Object getAllDeployments(HttpServletRequest req) {
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));
        int pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        List<DeploymentUseridDto> list = deploymentService.getAllDeployments(pageSize,pageNumber);
        int totalSize = deploymentService.getDeploymentCount();
        int totalPages = totalSize/pageSize+(totalSize%pageSize==0?0:1);
        JSONObject message = new JSONObject();
        message.put("pageSize",pageSize);
        message.put("pageNumber",pageNumber);
        message.put("totalPages",totalPages);
        message.put("deployDesc",list);
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/getOneDeployment", method = RequestMethod.POST)
    public Object getOneDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        Deployment deployment = deploymentService.getDeploymentBydeployId(deploymentId);
        JSONObject message = new JSONObject();
        Conf sourceConf = configureService.getConfigureByconfigureId(deployment.getSourceConfigureId());
        Conf targetConf = configureService.getConfigureByconfigureId(deployment.getTargetConfigureId());
        List<Process> processList = processService.getProcessList(deployment.getProcessIds());
        StringBuilder processNames = new StringBuilder();
        if(processList.size()==1){
            processNames.append(processList.get(0).getProcessName());
        }else{
            for(int i = 0;i<processList.size();i++){
                if(i!=processList.size()-1){
                    processNames.append(processList.get(i).getProcessName()+"\t");
                }else{
                    processNames.append(processList.get(i).getProcessName());
                }
            }
        }

        message.put("deploymentId",deploymentId);
        message.put("deploymentName",deployment.getDeploymentName());
        message.put("userId",deployment.getUserId());
        message.put("state",deployment.getState());
        message.put("updateTime",deployment.getUpdateTime());
        message.put("configureId",deployment.getUserId());
        message.put("sourceConfigureName",sourceConf.getConfigureName());
        message.put("targetConfigureName",targetConf.getConfigureName());
        message.put("processIds",deployment.getProcessIds());
        message.put("processNames",processNames.toString());

        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/addDeployment", method = RequestMethod.POST)
    public Object addDeployment(HttpServletRequest req) {
        String deploymentName = req.getParameter("deploymentName");
        String sourceConfigureId = req.getParameter("sourceConfigureId");
        String targetConfigureId = req.getParameter("targetConfigureId");
        String processIds = req.getParameter("processIds");
        User user = (User) req.getAttribute("user");
        String userId = user.getUserId();
        deploymentService.addDeployment(deploymentName,sourceConfigureId,targetConfigureId,processIds,userId);
        JSONObject message = new JSONObject();
        message.put("message","新增部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/deleteDeployment", method = RequestMethod.POST)
    public Object deleteDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        deploymentService.deleteDeployment(deploymentId);
        JSONObject message = new JSONObject();
        message.put("message","删除部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/updateDeployment", method = RequestMethod.POST)
    public Object updateDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        String deploymentName = req.getParameter("deploymentName");
        String sourceConfigureId = req.getParameter("sourceConfigureId");
        String targetConfigureId = req.getParameter("targetConfigureId");
        String processIds = req.getParameter("processIds");
        deploymentService.updateDeployment(deploymentId,deploymentName,sourceConfigureId,targetConfigureId,processIds);
        JSONObject message = new JSONObject();
        message.put("message","更新部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/startDeployment", method = RequestMethod.POST)
    public Object startDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        deploymentService.startDeployment(deploymentId);
        JSONObject message = new JSONObject();
        message.put("message","启动部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/stopDeployment", method = RequestMethod.POST)
    public Object stopDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        deploymentService.stopDeployment(deploymentId);
        JSONObject message = new JSONObject();
        message.put("message","暂停部署成功");
        return JSONObject.toJSON(message);
    }
}
