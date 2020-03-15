package com.km.data.core.statistics.plugin;

import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;

import java.util.List;
import java.util.Map;

/**
 * Created by jingxing on 14-9-9.
 */
public final class DefaultJobPluginCollector implements JobPluginCollector {
    private AbstractContainerCommunicator jobCollector;

    public DefaultJobPluginCollector(AbstractContainerCommunicator containerCollector) {
        this.jobCollector = containerCollector;
    }

    @Override
    public Map<String, List<String>> getMessage() {
        Communication totalCommunication = this.jobCollector.collect();
        return totalCommunication.getMessage();
    }

    @Override
    public List<String> getMessage(String key) {
        Communication totalCommunication = this.jobCollector.collect();
        return totalCommunication.getMessage(key);
    }
}
