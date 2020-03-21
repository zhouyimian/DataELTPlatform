package com.km.data.core.statistics.container.collector;



import com.km.data.common.util.Configuration;
import com.km.data.core.enums.State;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.communication.LocalTGCommunicationManager;
import com.km.data.core.util.container.CoreConstant;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCollector {
    private Map<Integer, Communication> taskCommunicationMap = new ConcurrentHashMap<Integer, Communication>();
    private Long jobId;
    private volatile LocalTGCommunicationManager TGCommunicationManager;

    public Map<Integer, Communication> getTaskCommunicationMap() {
        return taskCommunicationMap;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public LocalTGCommunicationManager registerTGCommunication(List<Configuration> taskGroupConfigurationList) {
        for (Configuration config : taskGroupConfigurationList) {
            int taskGroupId = config.getInt(
                    CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID);
            TGCommunicationManager.registerTaskGroupCommunication(taskGroupId, new Communication());
        }
        return TGCommunicationManager;
    }

    public void registerTaskCommunication(List<Configuration> taskConfigurationList) {
        for (Configuration taskConfig : taskConfigurationList) {
            int taskId = taskConfig.getInt(CoreConstant.TASK_ID);
            this.taskCommunicationMap.put(taskId, new Communication());
        }
    }

    public Communication collectFromTask() {
        Communication communication = new Communication();
        communication.setState(State.SUCCEEDED);

        for (Communication taskCommunication :
                this.taskCommunicationMap.values()) {
            communication.mergeFrom(taskCommunication);
        }

        return communication;
    }

    public abstract Communication collectFromTaskGroup();

    public Map<Integer, Communication> getTGCommunicationMap() {
        return TGCommunicationManager.getTaskGroupCommunicationMap();
    }

    public Communication getTGCommunication(Integer taskGroupId) {
        return TGCommunicationManager.getTaskGroupCommunication(taskGroupId);
    }

    public Communication getTaskCommunication(Integer taskId) {
        return this.taskCommunicationMap.get(taskId);
    }

    public LocalTGCommunicationManager getTGCommunicationManager() {
        return TGCommunicationManager;
    }

    public void setTGCommunicationManager(LocalTGCommunicationManager TGCommunicationManager) {
        this.TGCommunicationManager = TGCommunicationManager;
    }
}
