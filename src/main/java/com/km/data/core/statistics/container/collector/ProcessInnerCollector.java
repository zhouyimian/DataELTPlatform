package com.km.data.core.statistics.container.collector;

import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.communication.LocalTGCommunicationManager;

public class ProcessInnerCollector extends AbstractCollector {

    public ProcessInnerCollector(Long jobId) {
        super.setJobId(jobId);
    }

    @Override
    public Communication collectFromTaskGroup() {

        return super.getTGCommunicationManager().getJobCommunication();
    }
}
