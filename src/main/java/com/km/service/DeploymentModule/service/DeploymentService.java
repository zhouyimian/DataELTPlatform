package com.km.service.DeploymentModule.service;


import com.km.service.ConfigureModule.Mapper.ConfigureMapper;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ConfigureModule.service.ConfigureService;
import com.km.service.DataModule.DataService;
import com.km.service.DeploymentModule.Mapper.DeploymentMapper;
import com.km.service.DeploymentModule.domain.Deployment;
import com.km.service.DeploymentModule.dto.DeploymentUseridDto;
import com.km.service.ProcessModule.Mapper.ProcessMapper;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.common.exception.serviceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class DeploymentService {

    @Autowired
    DeploymentMapper deploymentMapper;

    @Autowired
    ConfigureMapper configureMapper;

    @Autowired
    ProcessMapper processMapper;

    @Autowired
    DataService dataService;


    public List<DeploymentUseridDto> getAllDeployments(int pageSize, int pageNumber) {
        int start = (pageNumber-1)*pageSize;
        return deploymentMapper.getAllDeployments(start,pageSize);
    }

    public int getDeploymentCount() {
        return deploymentMapper.getDeploymentCount();
    }

    public Deployment getDeploymentBydeployId(String deploymentId) {
        return deploymentMapper.getDeploymentBydeployId(deploymentId);
    }

    public void addDeployment(String deploymentName, String sourceConfigureId,String targetConfigureId,String processIds, String userId) {
        String state = "停止";
        String deploymentId = UUID.randomUUID().toString().replace("-","");
        Date nowDate = new Date();
        Deployment deployment = new Deployment();
        deployment.setDeploymentId(deploymentId);
        deployment.setState(state);
        deployment.setUpdateTime(nowDate);
        deployment.setDeploymentName(deploymentName);
        deployment.setUserId(userId);
        deployment.setSourceConfigureId(sourceConfigureId);
        deployment.setTargetConfigureId(targetConfigureId);
        deployment.setProcessIds(processIds);
        deploymentMapper.addDeployment(deployment);
    }

    public void deleteDeployment(String deploymentId) {
        Deployment deployment = deploymentMapper.getDeploymentBydeployId(deploymentId);
        if(!"停止".equals(deployment.getState())){
            throw new serviceException("该流程正在运行，无法删除");
        }
        deploymentMapper.deleteDeployment(deploymentId);
    }

    public void updateDeployment(String deploymentId, String deploymentName, String sourceConfigureId, String targetConfigureId,String processIds) {
        Deployment deployment = deploymentMapper.getDeploymentBydeployId(deploymentId);
        deployment.setDeploymentName(deploymentName);
        deployment.setSourceConfigureId(sourceConfigureId);
        deployment.setTargetConfigureId(targetConfigureId);
        deployment.setProcessIds(processIds);
        deployment.setUpdateTime(new Date());
        deploymentMapper.updateDeployment(deployment);
    }

    public void startDeployment(String deploymentId) {
        Deployment deployment = deploymentMapper.getDeploymentBydeployId(deploymentId);
        deployment.setState("运行中");
        deploymentMapper.updateDeployment(deployment);

        Conf sourceConf = configureMapper.getConfigureByconfigureId(deployment.getSourceConfigureId());
        sourceConf.setState("运行中");
        sourceConf.setRunningJobCount(sourceConf.getRunningJobCount()+1);
        configureMapper.updateConfigure(sourceConf);
        Conf targetConf = configureMapper.getConfigureByconfigureId(deployment.getTargetConfigureId());
        targetConf.setState("运行中");
        targetConf.setRunningJobCount(targetConf.getRunningJobCount()+1);
        configureMapper.updateConfigure(targetConf);


        String[] processIds = deployment.getProcessIds().split("\t");
        for(String processId:processIds){
            Process process = processMapper.getProcessByProcessId(processId);
            process.setState("运行中");
            process.setRunningJobCount(process.getRunningJobCount()+1);
            processMapper.updateProcess(process);
            dataService.startProcess(sourceConf,targetConf,process);
        }
    }

    public void stopDeployment(String deploymentId) {
        Deployment deployment = deploymentMapper.getDeploymentBydeployId(deploymentId);
        deployment.setState("停止");
        deploymentMapper.updateDeployment(deployment);

        Conf sourceConf = configureMapper.getConfigureByconfigureId(deployment.getSourceConfigureId());
        sourceConf.setRunningJobCount(sourceConf.getRunningJobCount()-1);
        if(sourceConf.getRunningJobCount()==0)
            sourceConf.setState("停止");
        configureMapper.updateConfigure(sourceConf);
        Conf targetConf = configureMapper.getConfigureByconfigureId(deployment.getTargetConfigureId());
        targetConf.setRunningJobCount(targetConf.getRunningJobCount()-1);
        if(targetConf.getRunningJobCount()==0)
            targetConf.setState("停止");
        configureMapper.updateConfigure(targetConf);


        String[] processIds = deployment.getProcessIds().split("\t");
        for(String processId:processIds){
            Process process = processMapper.getProcessByProcessId(processId);
            process.setRunningJobCount(process.getRunningJobCount()-1);
            if(process.getRunningJobCount()==0)
                process.setState("停止");
            processMapper.updateProcess(process);
        }
    }
}
