package com.km.data.core.statistics.container.communicator;


import com.km.data.common.util.Configuration;
import com.km.data.core.enums.State;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.container.collector.AbstractCollector;
import com.km.data.core.statistics.container.report.AbstractReporter;
import com.km.data.core.util.container.CoreConstant;

import java.util.List;
import java.util.Map;

public abstract class AbstractContainerCommunicator {
    private Configuration configuration;
    private AbstractCollector collector;
    private AbstractReporter reporter;

    private Long jobId;


    private long lastReportTime = System.currentTimeMillis();


    public Configuration getConfiguration() {
        return this.configuration;
    }

    public AbstractCollector getCollector() {
        return collector;
    }

    public AbstractReporter getReporter() {
        return reporter;
    }

    public void setCollector(AbstractCollector collector) {
        this.collector = collector;
    }

    public void setReporter(AbstractReporter reporter) {
        this.reporter = reporter;
    }

    public Long getJobId() {
        return jobId;
    }

    public AbstractContainerCommunicator(Configuration configuration) {
        this.configuration = configuration;
        this.jobId = configuration.getLong(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID);
    }


    public abstract void registerCommunication(List<Configuration> configurationList);

    public abstract Communication collect();

    public abstract void report(Communication communication);

    public abstract State collectState();

    public abstract Communication getCommunication(Integer id);

    /**
     * 当 实现是 TGContainerCommunicator 时，返回的 Map: key=taskId, value=Communication
     * 当 实现是 JobContainerCommunicator 时，返回的 Map: key=taskGroupId, value=Communication
     */
    public abstract Map<Integer, Communication> getCommunicationMap();

    public void resetCommunication(Integer id){
        Map<Integer, Communication> map = getCommunicationMap();
        map.put(id, new Communication());
    }
}