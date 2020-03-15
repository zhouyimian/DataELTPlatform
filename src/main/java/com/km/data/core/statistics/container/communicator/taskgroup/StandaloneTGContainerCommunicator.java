package com.km.data.core.statistics.container.communicator.taskgroup;

import com.km.data.common.util.Configuration;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.container.report.ProcessInnerReporter;

public class StandaloneTGContainerCommunicator extends AbstractTGContainerCommunicator {

    public StandaloneTGContainerCommunicator(Configuration configuration) {
        super(configuration);
        super.setReporter(new ProcessInnerReporter());
    }

    @Override
    public void report(Communication communication) {
        super.getReporter().reportTGCommunication(super.taskGroupId, communication);
    }

}
