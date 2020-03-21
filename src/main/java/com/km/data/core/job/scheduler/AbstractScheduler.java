package com.km.data.core.job.scheduler;

import com.km.data.common.exception.DataETLException;
import com.km.data.common.util.Configuration;
import com.km.data.common.util.FrameworkErrorCode;
import com.km.data.core.enums.State;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.communication.CommunicationTool;
import com.km.data.core.statistics.communication.LocalTGCommunicationManager;
import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.km.data.core.util.container.CoreConstant;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractScheduler {
    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractScheduler.class);

    private AbstractContainerCommunicator containerCommunicator;

    private Long jobId;

    public Long getJobId() {
        return jobId;
    }

    public AbstractScheduler(AbstractContainerCommunicator containerCommunicator) {
        this.containerCommunicator = containerCommunicator;
    }

    public void schedule(List<Configuration> configurations) {
        Validate.notNull(configurations,
                "scheduler配置不能为空");
        this.jobId = configurations.get(0).getLong(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_ID);

        /**
         * 给 taskGroupContainer 的 Communication 注册
         */
        this.containerCommunicator.registerCommunication(configurations);

        startAllTaskGroup(configurations,this.getContainerCommunicator().getCollector().getTGCommunicationManager());
    }

    protected abstract void startAllTaskGroup(List<Configuration> configurations, LocalTGCommunicationManager tgCommunicationManager);

    protected abstract void dealFailedStat(AbstractContainerCommunicator frameworkCollector, Throwable throwable);

    protected abstract void dealKillingStat(AbstractContainerCommunicator frameworkCollector, int totalTasks);

    private int calculateTaskCount(List<Configuration> configurations) {
        int totalTasks = 0;
        for (Configuration taskGroupConfiguration : configurations) {
            totalTasks += taskGroupConfiguration.getListConfiguration(
                    CoreConstant.DATAX_JOB_CONTENT).size();
        }
        return totalTasks;
    }

//    private boolean isJobKilling(Long jobId) {
//        Result<Integer> jobInfo = DataxServiceUtil.getJobInfo(jobId);
//        return jobInfo.getData() == State.KILLING.value();
//    }

    protected  abstract  boolean isJobKilling(Long jobId);

    public AbstractContainerCommunicator getContainerCommunicator() {
        return containerCommunicator;
    }
}
