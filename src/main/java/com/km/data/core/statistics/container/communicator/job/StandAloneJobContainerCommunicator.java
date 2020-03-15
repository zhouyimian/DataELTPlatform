package com.km.data.core.statistics.container.communicator.job;

import com.km.data.common.util.Configuration;
import com.km.data.core.enums.State;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.communication.CommunicationTool;
import com.km.data.core.statistics.container.collector.ProcessInnerCollector;
import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.km.data.core.statistics.container.report.ProcessInnerReporter;
import com.km.data.core.util.container.CoreConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class StandAloneJobContainerCommunicator extends AbstractContainerCommunicator {
    private static final Logger LOG = LoggerFactory
            .getLogger(StandAloneJobContainerCommunicator.class);

    public StandAloneJobContainerCommunicator(Configuration configuration) {
        super(configuration);
        super.setCollector(new ProcessInnerCollector(configuration.getLong(
                CoreConstant.DATAX_CORE_CONTAINER_JOB_ID)));
        super.setReporter(new ProcessInnerReporter());
    }

    @Override
    public void registerCommunication(List<Configuration> configurationList) {
        super.getCollector().registerTGCommunication(configurationList);
    }

    @Override
    public Communication collect() {
        return super.getCollector().collectFromTaskGroup();
    }

    @Override
    public State collectState() {
        return this.collect().getState();
    }

    /**
     * 和 DistributeJobContainerCollector 的 report 实现一样
     */
    @Override
    public void report(Communication communication) {
        super.getReporter().reportJobCommunication(super.getJobId(), communication);

        LOG.info(CommunicationTool.Stringify.getSnapshot(communication));
    }

    @Override
    public Communication getCommunication(Integer taskGroupId) {
        return super.getCollector().getTGCommunication(taskGroupId);
    }

    @Override
    public Map<Integer, Communication> getCommunicationMap() {
        return super.getCollector().getTGCommunicationMap();
    }
}
