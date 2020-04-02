package com.km.service.ConfigureModule.service;

import com.km.service.ConfigureModule.Mapper.ConfigureMapper;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ConfigureModule.dto.ConfUseridDto;
import com.km.service.common.exception.serviceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ConfigureService {

    @Autowired
    ConfigureMapper configureMapper;
    
    public List<ConfUseridDto> getAllConfigures(int pageSize, int pageNumber) {
        int start = (pageNumber-1)*pageSize;
        return configureMapper.getAllConfigures(start,pageSize);
    }

    public int getConfigureCount() {
        return configureMapper.getConfigureCount();
    }

    public Conf getConfigureByconfigureId(String configureId) {
        return configureMapper.getConfigureByconfigureId(configureId);
    }

    public void addConfigure(String configureType,String configureName, String configureContent,String configureStruct,String userId) {
        String state = "停止";
        String confId = UUID.randomUUID().toString().replace("-","");
        Date nowDate = new Date();
        Conf conf = new Conf();
        conf.setConfigureId(confId);
        conf.setConfigureType(configureType);
        conf.setState(state);
        conf.setUpdateTime(nowDate);
        conf.setConfigureContent(configureContent);
        conf.setUserId(userId);
        conf.setConfigureName(configureName);
        conf.setRunningJobCount(0);
        conf.setConfigureStruct(configureStruct);
        configureMapper.addConfigure(conf);
    }

    public void deleteConfigure(String configureId) {
        Conf conf = configureMapper.getConfigureByconfigureId(configureId);
        if(conf!=null&&conf.getRunningJobCount()!=0){
            throw new serviceException("目前有正在运行的任务绑定着该配置文件，无法删除");
        }
        configureMapper.deleteConfigure(configureId);
    }

    public void updateConfigure(String configureId, String configureName, String configureContent,String configureStruct) {
        Conf conf = configureMapper.getConfigureByconfigureId(configureId);
        if(conf!=null&&conf.getRunningJobCount()!=0){
            throw new serviceException("目前有正在运行的任务绑定着该配置文件，无法更新");
        }
        conf.setConfigureName(configureName);
        conf.setUpdateTime(new Date());
        conf.setConfigureContent(configureContent);
        conf.setConfigureStruct(configureStruct);
        configureMapper.updateConfigure(conf);
    }
}
