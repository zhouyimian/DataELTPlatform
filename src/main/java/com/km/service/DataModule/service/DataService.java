package com.km.service.DataModule.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.data.common.util.Configuration;
import com.km.data.core.Engine;
import com.km.data.core.enums.State;
import com.km.data.core.job.JobContainer;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.communication.CommunicationTool;
import com.km.data.core.statistics.communication.LocalTGCommunicationManager;
import com.km.data.etl.etlUtil.Key;
import com.km.service.ConfigureModule.Mapper.ConfigureMapper;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.DataModule.Mapper.JobMessageMapper;
import com.km.service.DataModule.domain.JobMessage;
import com.km.service.DeploymentModule.domain.Deployment;
import com.km.service.ProcessModule.Mapper.ProcessMapper;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.UserModule.domain.User;
import com.km.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DataService {

    @Autowired
    JobMessageMapper jobMessageMapper;

    @Autowired
    ConfigureMapper configureMapper;
    @Autowired
    ProcessMapper processMapper;


    public static Map<String,JobContainer> allJobContainerMap = new HashMap<>();

    public void startDeploy(String deploymentId,Conf sourceConf, Conf targetConf, Process process) {
        Configuration jobConfiguration = buildJobConf(sourceConf,targetConf,process);

        String corePath = "static/core.json";
        JSONObject corejson = JSONObject.parseObject(FileUtil.readFile(corePath));
        JSONObject jobjson = JSONObject.parseObject(jobConfiguration.toJSON());

        JSONObject mergeConfig = (JSONObject) corejson.clone();
        mergeConfig.putAll(jobjson);


        Engine engine = new Engine();
        JobContainer jobContainer = engine.start(new Configuration(mergeConfig.toJSONString()));
        allJobContainerMap.put(deploymentId,jobContainer);
    }


    public void stopDeploy(String deploymentId,boolean iskill) {
        JobContainer jobContainer = allJobContainerMap.get(deploymentId);
        if(jobContainer==null)
            return;
        LocalTGCommunicationManager localTGCommunicationManager =jobContainer.getContainerCommunicator().getCollector().getTGCommunicationManager();
        Set<Integer> ids = localTGCommunicationManager.getTaskGroupIdSet();
        if(iskill){
            for(Integer taskgroupId:ids){
                Communication communication = localTGCommunicationManager.getTaskGroupCommunication(taskgroupId);
                communication.setState(State.KILLED);
            }
        }else{
            for(Integer taskgroupId:ids){
                Communication communication = localTGCommunicationManager.getTaskGroupCommunication(taskgroupId);
                if(communication.getState()!=State.SUCCEEDED){
                    communication.setState(State.FAILED);
                }
            }
        }
    }


    private Configuration buildJobConf(Conf sourceConf, Conf targetConf, Process process) {
        Configuration jobconf = new Configuration("");
        jobconf.set("job.setting.speed.channel",5);
        jobconf.set("job.content.reader",JSONObject.parseObject(sourceConf.getConfigureContent()));
        jobconf.set("job.content.writer",JSONObject.parseObject(targetConf.getConfigureContent()));
        JSONArray processNodeArray = JSONArray.parseArray(process.getProcessContent());
        if(processNodeArray.size()==2){
            jobconf.set("job.content.ETL", "");
        }else{
            JSONArray etlArray = new JSONArray();
            for(int i = 1;i<processNodeArray.size()-1;i++){
                JSONObject node = processNodeArray.getJSONObject(i);
                JSONObject etlNode = new JSONObject();
                etlNode.put(Key.PLUGIN_NAME,node.getString(Key.PLUGIN_NAME));
                etlNode.put(Key.PLUGIN_CLASSPATH,node.getString(Key.PLUGIN_CLASSPATH));
                etlNode.put(Key.PLUGIN_PARAMETER,JSONObject.parseObject(node.getString(Key.PLUGIN_PARAMETER)));
                etlArray.add(etlNode);
            }
            jobconf.set("job.content.ETL", etlArray);
        }
        return jobconf;
    }


    public void addMessage(JobContainer jobContainer, Deployment deployment,User user) {
        JobMessage jobMessage = buildJobMessage(jobContainer,deployment,user);
        jobMessageMapper.addMessage(jobMessage);
    }

    private JobMessage buildJobMessage(JobContainer jobContainer,Deployment deployment,User user) {
        JobMessage jobMessage = new JobMessage();
        Communication communication = jobContainer.getContainerCommunicator().collect();
        Map<String, Number> counter = communication.getCounter();
        Conf sourceConf = configureMapper.getConfigureByconfigureId(deployment.getSourceConfigureId());
        Conf targetConf = configureMapper.getConfigureByconfigureId(deployment.getTargetConfigureId());
        Process process = processMapper.getProcessByProcessId(deployment.getProcessId());
        jobMessage.setJobMessageId(UUID.randomUUID().toString().replace("-",""));
        jobMessage.setSourceConfigureContent(sourceConf.getConfigureContent());
        jobMessage.setTargetConfigureContent(targetConf.getConfigureContent());
        jobMessage.setProcessContent(process.getProcessContent());
        jobMessage.setUserName(user.getUserName());
        jobMessage.setStartTime(jobContainer.getStartTimeStamp());
        jobMessage.setEndTime(jobContainer.getEndTimeStamp());
        jobMessage.setRecordTime(System.currentTimeMillis());
        jobMessage.setState(communication.getState());
        jobMessage.setThrowable(communication.getThrowable());
        jobMessage.setTaskNum(jobContainer.getTaskNumber());
        jobMessage.setFinishTasks(counter.getOrDefault(CommunicationTool.DONE_TASK_NUMBERS, 0).longValue());
        jobMessage.setReadSucceedRecords(counter.getOrDefault(CommunicationTool.READ_SUCCEED_RECORDS, 0).longValue());
        jobMessage.setReadSucceedBytes(counter.getOrDefault(CommunicationTool.READ_SUCCEED_BYTES, 0).longValue());
        jobMessage.setWriteSucceedRecords(counter.getOrDefault(CommunicationTool.WRITE_SUCCEED_RECORDS, 0).longValue());
        jobMessage.setWriteSucceedBytes(counter.getOrDefault(CommunicationTool.WRITE_SUCCEED_BYTES, 0).longValue());
        jobMessage.setTotalETLRecords(counter.getOrDefault(CommunicationTool.TOTAL_ETL_RECORDS, 0).longValue());
        jobMessage.setByteSpeed(counter.getOrDefault(CommunicationTool.BYTE_SPEED,0).longValue());
        jobMessage.setByteSpeed(counter.getOrDefault(CommunicationTool.RECORD_SPEED,0).longValue());

        return jobMessage;
    }


    public JSONObject getJobRunningCondition(String deploymentId) {
        JobContainer jobContainer = allJobContainerMap.get(deploymentId);
        return extractCommunicateInfo(jobContainer);
    }
    private JSONObject extractCommunicateInfo(JobContainer jobContainer) {
        JSONObject message = new JSONObject();
        message.put("taskNum",jobContainer.getTaskNumber());
        Communication communication = jobContainer.getContainerCommunicator().collect();
        message.put("state",communication.getState());
        Throwable throwable = communication.getThrowable();
        if(throwable!=null)
            message.put("exception",throwable.getMessage());
        for(Map.Entry<String,Number> entry:communication.getCounter().entrySet()){
            String key = entry.getKey();
            Number value = entry.getValue();
            message.put(key,value.longValue());
        }
        return message;
    }

    public JobContainer getJobContainer(String deploymentId) {
        return allJobContainerMap.get(deploymentId);
    }
}
