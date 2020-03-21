package com.km.data.core.statistics.container.report;

import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.communication.LocalTGCommunicationManager;

public class ProcessInnerReporter extends AbstractReporter {

    @Override
    public void reportJobCommunication(Long jobId, Communication communication) {
        // do nothing
    }

    @Override
    public void reportTGCommunication(Integer taskGroupId, Communication communication) {
        super.getTGCommunicationManager().updateTaskGroupCommunication(taskGroupId, communication);
    }

}