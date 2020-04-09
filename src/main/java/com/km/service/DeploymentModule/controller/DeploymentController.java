package com.km.service.DeploymentModule.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.data.core.job.JobContainer;
import com.km.data.core.statistics.communication.Communication;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ConfigureModule.service.ConfigureService;
import com.km.service.DeploymentModule.domain.Deployment;
import com.km.service.DeploymentModule.dto.DeploymentUseridDto;
import com.km.service.DeploymentModule.service.DeploymentService;
import com.km.service.JobModule.domain.JobReport;
import com.km.service.JobModule.service.DataService;
import com.km.service.PermissionModule.service.PermissionService;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.service.ProcessService;
import com.km.service.UserModule.domain.User;
import com.km.service.UserModule.service.UserService;
import com.km.service.common.exception.serviceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

@RestController
public class DeploymentController {

    @Autowired
    UserService userService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ConfigureService configureService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private DataService dataService;
    @Autowired
    private PermissionService permissionService;


    @RequestMapping(value = "/getAllDeployments", method = RequestMethod.POST)
    public Object getAllDeployments(HttpServletRequest req) {
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));
        int pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        List<DeploymentUseridDto> list = deploymentService.getAllDeployments(pageSize, pageNumber);
        int totalSize = deploymentService.getDeploymentCount();
        int totalPages = totalSize / pageSize + (totalSize % pageSize == 0 ? 0 : 1);
        JSONArray deployDesc = richDeployment(list);
        JSONObject message = new JSONObject();
        message.put("pageSize", pageSize);
        message.put("pageNumber", pageNumber);
        message.put("totalPages", totalPages);
        message.put("deployDesc", deployDesc);

        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/getAllPrivateDeployments", method = RequestMethod.POST)
    public Object getAllPrivateDeployments(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        JSONObject message = new JSONObject();
        JSONArray deployDesc;
        List<DeploymentUseridDto> list;
        int pageSize;
        int pageNumber;
        int totalPages;
        if (req.getParameter("pageSize") == null && req.getParameter("pageNumber") == null) {
            list = deploymentService.getAllPrivateDeployments(user.getUserId());
            pageSize = list.size();
            pageNumber = 1;
            totalPages = 1;
        } else {
            pageSize = Integer.parseInt(req.getParameter("pageSize"));
            pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
            list = deploymentService.getPagePrivateDeployments(user.getUserId(), pageSize, pageNumber);
            int totalSize = deploymentService.getPrivateDeploymentCount(user.getUserId());
            totalPages = totalSize / pageSize + (totalSize % pageSize == 0 ? 0 : 1);
        }
        deployDesc = richDeployment(list);
        message.put("pageSize", pageSize);
        message.put("pageNumber", pageNumber);
        message.put("totalPages", totalPages);
        message.put("deployDesc", deployDesc);
        return JSONObject.toJSON(message);
    }

    private JSONArray richDeployment(List<DeploymentUseridDto> list) {
        JSONArray result = new JSONArray();
        for (DeploymentUseridDto dto : list) {
            JSONObject object = JSONObject.parseObject(JSONObject.toJSON(dto).toString());
            Process process = processService.getProcessByProcessId(dto.getProcessId());
            JSONArray jsonArray = JSONArray.parseArray(process.getProcessContent());
            int size = jsonArray.size();
            object.put("input", jsonArray.getJSONObject(0).getString("pluginName"));
            object.put("output", jsonArray.getJSONObject(size - 1).getString("pluginName"));
            object.put("processName", process.getProcessName());
            result.add(object);
        }
        return result;
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
        User user = (User) req.getAttribute("user");
        JSONObject message = new JSONObject();
        boolean isNotPermission = false;
        for (int i = 0; i < deploymentIds.size(); i++) {
            Deployment deployment = deploymentService.getDeploymentBydeployId(deploymentIds.getString(i));
            if (!deployment.getUserId().equals(user.getUserId())) {
                isNotPermission = true;
                break;
            }
        }
        if (isNotPermission) {
            throw new serviceException("要删除的部署中有部分部署无删除权限!");
        } else {
            for (int i = 0; i < deploymentIds.size(); i++)
                deploymentService.deleteDeployment(deploymentIds.getString(i));
            message.put("message", "批量删除部署成功");
        }
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
        User user = (User) req.getAttribute("user");
        deploymentService.startDeployment(deploymentId, user.getUserId());
        JSONObject message = new JSONObject();
        message.put("message", "启动部署成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/stopDeployment", method = RequestMethod.POST)
    public Object stopDeployment(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        User user = (User) req.getAttribute("user");
        JSONObject runningInformation = deploymentService.getRunningInformation(deploymentId);
        String state = runningInformation.getString("state");
        if (!isFinish(state)) {
            deploymentService.stopDeployment(deploymentId);
            deploymentService.dealAfterFinishDeployment(deploymentId);
            JobReport jobReport = dataService.buildJobReport(deploymentService.getDeploymentBydeployId(deploymentId));
            jobReport.setStopUserId(user.getUserId());
            jobReport.setStopUserName(user.getUserName());
            dataService.saveJobReport(jobReport);
            dataService.removeJobContainer(deploymentId);
        }else{
            deploymentService.dealAfterFinishDeployment(deploymentId);
            JobReport jobReport = dataService.buildJobReport(deploymentService.getDeploymentBydeployId(deploymentId));
            dataService.saveJobReport(jobReport);
            dataService.removeJobContainer(deploymentId);
            throw new serviceException("该任务已经结束,无法停止,可以前往任务报告查看任务详细信息");
        }
        JSONObject message = new JSONObject();
        message.put("message", "暂停部署成功");
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/getRunningInformation", method = RequestMethod.POST)
    public Object getRunningInformation(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        Deployment deployment = deploymentService.getDeploymentBydeployId(deploymentId);
        JSONObject message = deploymentService.getRunningInformation(deploymentId);
        String state = message.getString("state");
        if (isFinish(state)) {
            deploymentService.dealAfterFinishDeployment(deploymentId);
            JobReport jobReport = dataService.buildJobReport(deployment);
            dataService.saveJobReport(jobReport);
            dataService.removeJobContainer(deploymentId);
        }
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/getRunningDeploymentIds", method = RequestMethod.POST)
    public Object getRunningDeploymentIds(HttpServletRequest req) {
        List<String> runningDeploymentIds = dataService.getRunningDeploymentIds();
        Iterator<String> iterator = runningDeploymentIds.iterator();
        while (iterator.hasNext()){
            String deploymentId = iterator.next();
            JobContainer container = dataService.getJobContainer(deploymentId);
            Communication communication = container.getContainerCommunicator().collect();
            if (communication.getState().isFinished()) {
                deploymentService.dealAfterFinishDeployment(deploymentId);
                JobReport jobReport = dataService.buildJobReport(deploymentService.getDeploymentBydeployId(deploymentId));
                dataService.saveJobReport(jobReport);
                dataService.removeJobContainer(deploymentId);
                iterator.remove();
            }
        }
        return JSONObject.toJSON(runningDeploymentIds);
    }


    /**
     * 获取当前用户被授权的所有部署的详细信息
     * @param req
     * @return
     */
    @RequestMapping(value = "/getUserAuthorizedDeployments", method = RequestMethod.POST)
    public Object getUserAuthorizedDeployments(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        List<DeploymentUseridDto> list = deploymentService.getUserAuthorizedDeployments(user.getUserId());
        JSONArray result = richDeployment(list);
        JSONObject message = new JSONObject();
        message.put("deployDesc",result);
        return JSONObject.toJSON(message);
    }

    /**
     * 获取当前用户所拥有的每个部署的被授权用户信息
     * @param req
     * @return
     */
    @RequestMapping(value = "/getDeploymentPermissionUserSituation", method = RequestMethod.POST)
    public Object getDeploymentPermissionUserSituation(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        List<DeploymentUseridDto> list = deploymentService.getAllPrivateDeployments(user.getUserId());
        JSONArray result = new JSONArray();
        for(DeploymentUseridDto dto:list){
            List<User> users = permissionService.getAllDeploymentPermissionUser(dto.getDeploymentId());
            if(users.size()==0)
                continue;
            JSONObject object = new JSONObject();
            object.put("deploymentId",dto.getDeploymentId());
            object.put("deploymentName",dto.getDeploymentName());
            object.put("content",JSONArray.parseArray(JSON.toJSONString(users)));
            result.add(object);
        }
        JSONObject message = new JSONObject();
        message.put("situation",result);
        return JSONObject.toJSON(message);
    }

    private boolean isFinish(String state) {
        return state.equals("KILLED") || state.equals("FAILED") || state.equals("SUCCEEDED");
    }




}
