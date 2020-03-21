package com.km.data.core.statistics.container.report;


import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.communication.LocalTGCommunicationManager;

public abstract class AbstractReporter {
    private LocalTGCommunicationManager TGCommunicationManager;

    public abstract void reportJobCommunication(Long jobId, Communication communication);

    public abstract void reportTGCommunication(Integer taskGroupId, Communication communication);

    public LocalTGCommunicationManager getTGCommunicationManager() {
        return TGCommunicationManager;
    }

    public void setTGCommunicationManager(LocalTGCommunicationManager TGCommunicationManager) {
        this.TGCommunicationManager = TGCommunicationManager;
    }
}
