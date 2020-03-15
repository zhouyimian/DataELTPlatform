package com.km.service.ProcessModule.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.data.common.util.Configuration;
import com.km.service.PermissionModule.service.PermissionService;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.dto.ProcessUseridDto;
import com.km.service.ProcessModule.service.ProcessService;
import com.km.service.UserModule.domain.User;
import com.km.service.common.UnAuthToken;
import com.km.service.common.utils.RedisUtil;
import com.km.utils.LoadConfigureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ProcessController {

    @Autowired
    private ProcessService processService;


    @RequestMapping(value = "/getAllProcess", method = RequestMethod.POST)
    public Object getAllProcess(HttpServletRequest req) {
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));
        int pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        List<ProcessUseridDto> list = processService.getAllProcess(pageSize,pageNumber);
        int totalSize = processService.getProcessCount();
        int totalPages = totalSize/pageSize+(totalSize%pageSize==0?0:1);
        JSONObject message = new JSONObject();
        message.put("pageSize",pageSize);
        message.put("pageNumber",pageNumber);
        message.put("totalPages",totalPages);
        message.put("processDesc",list);
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/getAllPlugins", method = RequestMethod.POST)
    public Object getAllPlugins(HttpServletRequest req) {
        Map<String, Configuration> reader = LoadConfigureUtil.getReaderPlugNameToConf();
        Map<String, Configuration> writer = LoadConfigureUtil.getWriterPlugNameToConf();
        Map<String, Configuration> etl = LoadConfigureUtil.getEtlPlugNameToConf();
        JSONObject message = new JSONObject();
        message.put("readerPlugins",parseMap(reader));
        message.put("writerPlugins",parseMap(writer));
        message.put("etlPlugins",parseMap(etl));
        return JSONObject.toJSON(message);
    }



    @RequestMapping(value = "/getOneProcess", method = RequestMethod.POST)
    public Object getOneProcessDesc(HttpServletRequest req) {
        String processId = req.getParameter("processId");
        Process process = processService.getProcessByProcessId(processId);
        return JSONObject.toJSON(process);
    }

    @RequestMapping(value = "/addProcess", method = RequestMethod.POST)
    public Object addProcess(HttpServletRequest req) {
        String processName = req.getParameter("processName");
        String processContent = req.getParameter("processContent");
        User user = (User) req.getAttribute("user");
        String userId = user.getUserId();
        processService.addProcess(processName,processContent,userId);
        JSONObject message = new JSONObject();
        message.put("message","创建流程成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/deleteProcess", method = RequestMethod.POST)
    public Object deleteProcess(HttpServletRequest req) {
        String processId = req.getParameter("processId");
        processService.deleteProcess(processId);
        JSONObject message = new JSONObject();
        message.put("message","删除流程成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/updateProcess", method = RequestMethod.POST)
    public Object updateProcess(HttpServletRequest req) {
        String processId = req.getParameter("processId");
        String processName = req.getParameter("processName");
        String processContent = req.getParameter("processContent");
        processService.updateProcess(processId,processName,processContent);
        JSONObject message = new JSONObject();
        message.put("message","更新流程成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/exportProcess", method = RequestMethod.POST)
    public Object exportProcess(HttpServletRequest req) {
        String processId = req.getParameter("processId");
        Process process = processService.getProcessByProcessId(processId);
        JSONObject message = new JSONObject();
        if(null!=message) {
            message.put("processName", process.getProcessName());
            message.put("processContent", process.getProcessContent());
        }
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/importProcess", method = RequestMethod.POST)
    public Object importProcess(HttpServletRequest req) {
        String processName = req.getParameter("processName");
        String processContent = req.getParameter("processContent");
        User user = (User) req.getAttribute("user");
        String userId = user.getUserId();
        processService.addProcess(processName,processContent,userId);
        JSONObject message = new JSONObject();
        message.put("message","导入流程成功");
        return JSONObject.toJSON(message);
    }

    private Object parseMap(Map<String, Configuration> configurationMap) {
        JSONArray array = new JSONArray();
        for(Map.Entry<String,Configuration> entry:configurationMap.entrySet()){
            Configuration value = entry.getValue();
            array.add(JSONObject.parseObject(value.toJSON()));
        }
        return JSONObject.toJSON(array);
    }
}
