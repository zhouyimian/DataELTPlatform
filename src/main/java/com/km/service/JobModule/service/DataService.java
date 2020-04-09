package com.km.service.JobModule.service;

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
import com.km.service.DeploymentModule.domain.Deployment;
import com.km.service.DeploymentModule.service.DeploymentService;
import com.km.service.JobModule.Mapper.JobReportMapper;
import com.km.service.JobModule.domain.JobReport;
import com.km.service.ProcessModule.Mapper.ProcessMapper;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.UserModule.domain.User;
import com.km.service.UserModule.service.UserService;
import com.km.service.common.exception.serviceException;
import com.km.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataService {

    @Autowired
    JobReportMapper jobReportMapper;
    @Autowired
    ConfigureMapper configureMapper;
    @Autowired
    ProcessMapper processMapper;

    @Autowired
    UserService userService;
    @Autowired
    DeploymentService deploymentService;


    public static Map<String, JobContainer> allJobContainerMap = new ConcurrentHashMap<>();
    public static Map<String, String> jobStarter = new ConcurrentHashMap<>();

    public void startDeploy(String startUser,String deploymentId, Conf sourceConf, Conf targetConf, Process process) {
        jobStarter.put(deploymentId, startUser);
        Configuration jobConfiguration = buildJobConf(sourceConf, targetConf, process);

        String corePath = "static/core.json";
        JSONObject corejson = JSONObject.parseObject(FileUtil.readFile(corePath));
        JSONObject jobjson = JSONObject.parseObject(jobConfiguration.toJSON());

        JSONObject mergeConfig = (JSONObject) corejson.clone();
        mergeConfig.putAll(jobjson);


        Engine engine = new Engine();
        JobContainer jobContainer = engine.start(new Configuration(mergeConfig.toJSONString()));
        allJobContainerMap.put(deploymentId, jobContainer);
    }


    public void stopDeployment(Deployment deployment) {
        JobContainer jobContainer = allJobContainerMap.get(deployment.getDeploymentId());
        if (jobContainer == null)
            return;
        LocalTGCommunicationManager localTGCommunicationManager = jobContainer.getContainerCommunicator().getCollector().getTGCommunicationManager();
        Set<Integer> ids = localTGCommunicationManager.getTaskGroupIdSet();
        for (Integer taskgroupId : ids) {
            Communication communication = localTGCommunicationManager.getTaskGroupCommunication(taskgroupId);
            communication.setState(State.KILLED);
        }
        jobContainer.setEndTimeStamp(System.currentTimeMillis());
    }


    private Configuration buildJobConf(Conf sourceConf, Conf targetConf, Process process) {
        Configuration jobconf = new Configuration("");
        jobconf.set("job.setting.speed.channel", 5);
        jobconf.set("job.content.reader", JSONObject.parseObject(sourceConf.getConfigureContent()));
        jobconf.set("job.content.writer", JSONObject.parseObject(targetConf.getConfigureContent()));
        JSONArray processNodeArray = JSONArray.parseArray(process.getProcessContent());
        if (processNodeArray.size() > 2) {
            JSONArray etlArray = new JSONArray();
            for (int i = 1; i < processNodeArray.size() - 1; i++) {
                JSONObject node = processNodeArray.getJSONObject(i);
                JSONObject etlNode = new JSONObject();
                etlNode.put(Key.PLUGIN_NAME, node.getString(Key.PLUGIN_NAME));
                etlNode.put(Key.PLUGIN_CLASSPATH, node.getString(Key.PLUGIN_CLASSPATH));
                etlNode.put(Key.PLUGIN_PARAMETER, JSONObject.parseObject(node.getString(Key.PLUGIN_PARAMETER)));
                etlArray.add(etlNode);
            }
            jobconf.set("job.content.ETL", etlArray);
        }
        return jobconf;
    }



    public JSONObject getJobRunningCondition(String deploymentId) {
        JobContainer jobContainer = allJobContainerMap.get(deploymentId);
        if(jobContainer==null){
            throw new serviceException("该部署不存在,或者已经运行完毕,可以前往任务报告查看具体情况");
        }
        return extractCommunicateInfo(jobContainer);
    }

    private JSONObject extractCommunicateInfo(JobContainer jobContainer) {
        JSONObject message = new JSONObject();
        message.put("taskNum", jobContainer.getTaskNumber());
        Communication communication = jobContainer.getContainerCommunicator().collect();
        message.put("state", communication.getState());
        Throwable throwable = communication.getThrowable();
        if (throwable != null)
            message.put("exception", throwable.getMessage());
        for (Map.Entry<String, Number> entry : communication.getCounter().entrySet()) {
            String key = entry.getKey();
            Number value = entry.getValue();
            message.put(key, value.longValue());
        }
        return message;
    }

    public JobContainer getJobContainer(String deploymentId) {
        return allJobContainerMap.get(deploymentId);
    }


    public void removeJobContainer(String deploymentId) {
        allJobContainerMap.remove(deploymentId);
        jobStarter.remove(deploymentId);
    }

    public JobReport buildJobReport(Deployment deployment) {
        JobContainer jobContainer = allJobContainerMap.get(deployment.getDeploymentId());
        JobReport jobReport = new JobReport();
        Communication communication = jobContainer.getContainerCommunicator().collect();
        Map<String, Number> counter = communication.getCounter();
        Conf sourceConf = configureMapper.getConfigureByconfigureId(deployment.getSourceConfigureId());
        Conf targetConf = configureMapper.getConfigureByconfigureId(deployment.getTargetConfigureId());
        Process process = processMapper.getProcessByProcessId(deployment.getProcessId());
        User startUser = userService.getUserByUserId(jobStarter.get(deployment.getDeploymentId()));

        jobReport.setJobReportId(UUID.randomUUID().toString().replace("-", ""));
        jobReport.setSourceConfigureContent(sourceConf.getConfigureContent());
        jobReport.setSourceConfigureStruct(sourceConf.getConfigureStruct());
        jobReport.setTargetConfigureContent(targetConf.getConfigureContent());
        jobReport.setTargetConfigureStruct(targetConf.getConfigureStruct());
        jobReport.setProcessContent(process.getProcessContent());
        jobReport.setDeploymentId(deployment.getDeploymentId());
        jobReport.setDeploymentName(deployment.getDeploymentName());
        jobReport.setDeploymentContainerId(deployment.getUserId());
        jobReport.setDeploymentContainerName(userService.getUserByUserId(deployment.getUserId()).getUserName());
        jobReport.setStartUserId(startUser.getUserId());
        jobReport.setStartUserName(startUser.getUserName());

        jobReport.setStartTime(new Date(jobContainer.getStartTimeStamp()));
        jobReport.setEndTime(new Date(jobContainer.getEndTimeStamp()));
        long useTime = jobContainer.getEndTimeStamp()-jobContainer.getStartTimeStamp();
        if(useTime<1000)
            useTime = 1000;

        jobReport.setRecordTime(new Date());
        jobReport.setState(communication.getState().toString());
        if(communication.getThrowable()!=null)
            jobReport.setThrowable(communication.getThrowable().getMessage());
        jobReport.setTaskNum(jobContainer.getTaskNumber());

        jobReport.setFinishTasks(counter.getOrDefault(CommunicationTool.DONE_TASK_NUMBERS, 0).intValue());
        jobReport.setReadSucceedRecords(counter.getOrDefault(CommunicationTool.READ_SUCCEED_RECORDS, 0).longValue());
        jobReport.setReadSucceedBytes(counter.getOrDefault(CommunicationTool.READ_SUCCEED_BYTES, 0).longValue());
        jobReport.setWriteSucceedRecords(counter.getOrDefault(CommunicationTool.WRITE_SUCCEED_RECORDS, 0).longValue());
        jobReport.setWriteSucceedBytes(counter.getOrDefault(CommunicationTool.WRITE_SUCCEED_BYTES, 0).longValue());
        jobReport.setTotalInputEtlRecords(counter.getOrDefault(CommunicationTool.TOTAL_INPUT_ETL_RECORDS, 0).longValue());
        jobReport.setTotalOutputEtlRecords(counter.getOrDefault(CommunicationTool.TOTAL_OUTPUT_ETL_RECORDS, 0).longValue());


        jobReport.setAverageByteSpeed(jobReport.getReadSucceedBytes()/(useTime/1000));
        jobReport.setAverageRecordSpeed(jobReport.getReadSucceedRecords()/(useTime/1000));

        return jobReport;
    }

    public void saveJobReport(JobReport jobReport) {
        jobReportMapper.saveJobReport(jobReport);
    }

    public List<String> getRunningDeploymentIds() {
        List<String> deployments = new ArrayList<>();
        for (Map.Entry<String, JobContainer> entry : allJobContainerMap.entrySet()) {
            String deploymentId = entry.getKey();
            deployments.add(deploymentId);
        }
        return deployments;
    }

    public JobReport getJobReportByJobReportId(String jobReportId) {
        return jobReportMapper.getJobReportByJobReportId(jobReportId);
    }

    public int getJobReportCount() {
        return jobReportMapper.getJobReportCount();
    }

    public List<JobReport> getAllJobReports(int pageSize, int pageNumber) {
        int start = (pageNumber-1)*pageSize;
        return jobReportMapper.getAllJobReports(start,pageSize);
    }

    public List<JobReport> getAllPrivateJobReports(String userId) {
        return jobReportMapper.getAllPrivateJobReports(userId);
    }

    public List<JobReport> getPagePrivateJobReports(String userId, int pageSize, int pageNumber) {
        int start = (pageNumber-1)*pageSize;
        return jobReportMapper.getPagePrivateJobReports(userId,start,pageSize);
    }

    public int getPrivateJobReportCount(String userId) {
        return jobReportMapper.getPrivateJobReportCount(userId);
    }
}
