package com.km.service.ConfigureModule.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ConfigureModule.dto.ConfUseridDto;
import com.km.service.ConfigureModule.service.ConfigureService;
import com.km.service.UserModule.domain.User;
import com.km.service.common.exception.serviceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ConfigureController {

    @Autowired
    private ConfigureService configureService;

    @RequestMapping(value = "/getAllConfigures", method = RequestMethod.POST)
    public Object getAllConfigures(HttpServletRequest req) {
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));
        int pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        List<ConfUseridDto> list = configureService.getAllConfigures(pageSize,pageNumber);
        int totalSize = configureService.getConfigureCount();
        int totalPages = totalSize/pageSize+(totalSize%pageSize==0?0:1);
        JSONObject message = new JSONObject();
        message.put("pageSize",pageSize);
        message.put("pageNumber",pageNumber);
        message.put("totalPages",totalPages);
        message.put("confDesc",list);
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/getOneConfigure", method = RequestMethod.POST)
    public Object getOneConfigure(HttpServletRequest req) {
        String configureId = req.getParameter("configureId");
        Conf conf = configureService.getConfigureByconfigureId(configureId);
        return JSONObject.toJSON(conf);
    }

    @RequestMapping(value = "/addConfigure", method = RequestMethod.POST)
    public Object addConfigure(HttpServletRequest req) {
        String configureName = req.getParameter("configureName");
        String configureContent = req.getParameter("configureContent");
        String configureType = req.getParameter("configureType");
        String configureStruct = req.getParameter("configureStruct");
        User user = (User) req.getAttribute("user");
        String userId = user.getUserId();
        configureService.addConfigure(configureType,configureName,configureContent,configureStruct,userId);
        JSONObject message = new JSONObject();
        message.put("message","新增配置文件成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/deleteConfigure", method = RequestMethod.POST)
    public Object deleteConfigure(HttpServletRequest req) {
        String configureId = req.getParameter("configureId");
        configureService.deleteConfigure(configureId);
        JSONObject message = new JSONObject();
        message.put("message","删除配置文件成功");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/batchDeleteConfigure", method = RequestMethod.POST)
    public Object batchDeleteConfigure(HttpServletRequest req) {
        String ids = req.getParameter("configureIds");
        User user = (User) req.getAttribute("user");
        JSONArray configureIds = JSONArray.parseArray(ids);
        JSONObject message = new JSONObject();
        boolean isNotPermission = false;
        for (int i = 0; i < configureIds.size(); i++) {
            Conf conf = configureService.getConfigureByconfigureId(configureIds.get(i).toString());
            if (!conf.getUserId().equals(user.getUserId())) {
                isNotPermission = true;
                break;
            }
        }
        if(isNotPermission){
            throw new serviceException("要删除的配置文件中有部分无删除权限!");
        }else{
            for(int i = 0;i<configureIds.size();i++)
                configureService.deleteConfigure(configureIds.getString(i));
            message.put("message","批量删除配置文件成功");
        }
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/updateConfigure", method = RequestMethod.POST)
    public Object updateConfigure(HttpServletRequest req) {
        String configureId = req.getParameter("configureId");
        String configureName = req.getParameter("configureName");
        String configureContent = req.getParameter("configureContent");
        String configureStruct = req.getParameter("configureStruct");
        configureService.updateConfigure(configureId,configureName,configureContent,configureStruct);
        JSONObject message = new JSONObject();
        message.put("message","更新配置文件成功");
        return JSONObject.toJSON(message);
    }

    /**
     * 导出配置文件
     * @param req
     * @return
     */
    @RequestMapping(value = "/exportConfigure", method = RequestMethod.POST)
    public Object exportConfigure(HttpServletRequest req) {
        String ids = req.getParameter("configureIds");
        JSONArray configureIds = JSONArray.parseArray(ids);
        JSONArray confJSONArray = new JSONArray();
        for(int i = 0;i<configureIds.size();i++){
            Conf conf = configureService.getConfigureByconfigureId(configureIds.get(i).toString());
            JSONObject message = new JSONObject();
            message.put("configureType", conf.getConfigureType());
            message.put("configureName", conf.getConfigureName());
            message.put("configureContent", conf.getConfigureContent());
            message.put("configureStruct", conf.getConfigureStruct());
            confJSONArray.add(message);
        }
        JSONObject message = new JSONObject();
        message.put("contents",confJSONArray.toJSONString());
        return JSONObject.toJSON(message);
    }

    /**
     * 导入配置文件
     * @param req
     * @return
     */
    @RequestMapping(value = "/importConfigure", method = RequestMethod.POST)
    public Object importConfigure(HttpServletRequest req) {
        String contents = req.getParameter("configures");
        JSONArray confContents = JSONArray.parseArray(contents);
        User user = (User) req.getAttribute("user");
        String userId = user.getUserId();
        for(int i = 0;i<confContents.size();i++){
            JSONObject oneConf = confContents.getJSONObject(i);
            String configureType = oneConf.getString("configureType");
            String configureName = oneConf.getString("configureName");
            String configureContent = oneConf.getString("configureContent");
            String configureStruct = oneConf.getString("configureStruct");
            configureService.addConfigure(configureType,configureName,configureContent,configureStruct,userId);
        }
        JSONObject message = new JSONObject();
        message.put("message","导入配置文件成功");
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/getAllPrivateConfigures", method = RequestMethod.POST)
    public Object getAllPrivateConfigures(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        List<ConfUseridDto> list;
        JSONObject message = new JSONObject();
        int pageSize;
        int pageNumber;
        int totalPages;
        if (req.getParameter("pageSize") == null && req.getParameter("pageNumber") == null) {
            list = configureService.getAllPrivateConfigures(user.getUserId());
            pageSize = list.size();
            pageNumber = 1;
            totalPages = 1;
        }else{
            pageSize = Integer.parseInt(req.getParameter("pageSize"));
            pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
            list = configureService.getPagePrivateConfigures(user.getUserId(),pageSize,pageNumber);
            int totalSize = configureService.getPrivateConfigureCount(user.getUserId());
            totalPages = totalSize/pageSize+(totalSize%pageSize==0?0:1);
        }
        message.put("pageSize",pageSize);
        message.put("pageNumber",pageNumber);
        message.put("totalPages",totalPages);
        message.put("confDesc",list);
        return JSONObject.toJSON(message);
    }
}
