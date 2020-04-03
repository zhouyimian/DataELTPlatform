package com.km.service.DeploymentModule.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ConfigureModule.service.ConfigureService;
import com.km.service.DeploymentModule.domain.Deployment;
import com.km.service.DeploymentModule.dto.DeploymentUseridDto;
import com.km.service.DeploymentModule.service.DeploymentService;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.service.ProcessService;
import com.km.service.UserModule.domain.User;
import com.km.service.common.CommunicateInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<DeploymentUseridDto> list = deploymentService.getAllDeployments(pageSize, pageNumber);
        int totalSize = deploymentService.getDeploymentCount();
        int totalPages = totalSize / pageSize + (totalSize % pageSize == 0 ? 0 : 1);
        JSONObject message = new JSONObject();
        message.put("pageSize", pageSize);
        message.put("pageNumber", pageNumber);
        message.put("totalPages", totalPages);
        message.put("deployDesc", list);

        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/getOneDeployment", method = RequestMethod.POST)
    public Object getOneDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        Deployment deployment = deploymentService.getDeploymentBydeployId(deploymentId);
        JSONObject message = new JSONObject();
        Conf sourceConf = configureService.getConfigureByconfigureId(deployment.getSourceConfigureId());
        Conf targetConf = configureService.getConfigureByconfigureId(deployment.getTargetConfigureId());
        Process process = processService.getProcessByProcessId(deployment.getProcessId());
        message.put("deploymentId", deploymentId);
        message.put("deploymentName", deployment.getDeploymentName());
        message.put("userId", deployment.getUserId());
        message.put("state", deployment.getState());
        message.put("updateTime", deployment.getUpdateTime());
        message.put("sourceConfigureId", sourceConf.getConfigureId());
        message.put("targetConfigureId", targetConf.getConfigureId());
        message.put("sourceConfigureName", sourceConf.getConfigureName());
        message.put("targetConfigureName", targetConf.getConfigureName());
        message.put("processId", deployment.getProcessId());
        message.put("processName", process.getProcessName());
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/addDeployment", method = RequestMethod.POST)
    public Object addDeployment(HttpServletRequest req) {
        String deploymentName = req.getParameter("deploymentName");
        String sourceConfigureId = req.getParameter("sourceConfigureId");
        String targetConfigureId = req.getParameter("targetConfigureId");
        String processId = req.getParameter("processId");
        User user = (User) req.getAttribute("user");
        String userId = user.getUserId();
        deploymentService.addDeployment(deploymentName, sourceConfigureId, targetConfigureId, processId, userId);
        JSONObject message = new JSONObject();
        message.put("message", "新增部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/deleteDeployment", method = RequestMethod.POST)
    public Object deleteDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        deploymentService.deleteDeployment(deploymentId);
        JSONObject message = new JSONObject();
        message.put("message", "删除部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/batchDeleteDeployment", method = RequestMethod.POST)
    public Object batchDeleteDeployment(HttpServletRequest req) {
        String ids = req.getParameter("deploymentIds");
        JSONArray deploymentIds = JSONArray.parseArray(ids);
        for (int i = 0; i < deploymentIds.size(); i++) {
            deploymentService.deleteDeployment(deploymentIds.get(i).toString());
        }
        JSONObject message = new JSONObject();
        message.put("message", "批量删除部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/updateDeployment", method = RequestMethod.POST)
    public Object updateDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        String deploymentName = req.getParameter("deploymentName");
        String sourceConfigureId = req.getParameter("sourceConfigureId");
        String targetConfigureId = req.getParameter("targetConfigureId");
        String processId = req.getParameter("processId");
        deploymentService.updateDeployment(deploymentId, deploymentName, sourceConfigureId, targetConfigureId, processId);
        JSONObject message = new JSONObject();
        message.put("message", "更新部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/startDeployment", method = RequestMethod.POST)
    public Object startDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        deploymentService.startDeployment(deploymentId);
        JSONObject message = new JSONObject();
        message.put("message", "启动部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/stopDeployment", method = RequestMethod.POST)
    public Object stopDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        deploymentService.stopDeployment(deploymentId);
        JSONObject message = new JSONObject();
        message.put("message", "暂停部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/getRunningInformation", method = RequestMethod.POST)
    public Object getRunningInformation(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        JSONObject message = deploymentService.getRunningInformation(deploymentId);
        return JSONObject.toJSON(message);
    }

}
