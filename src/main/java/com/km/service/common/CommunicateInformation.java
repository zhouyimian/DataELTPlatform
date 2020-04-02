package com.km.service.common;

import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;

public class CommunicateInformation {
    String deploymentId;
    AbstractContainerCommunicator containerCommunicator;
    int taskNum;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public AbstractContainerCommunicator getContainerCommunicator() {
        return containerCommunicator;
    }

    public void setContainerCommunicator(AbstractContainerCommunicator containerCommunicator) {
        this.containerCommunicator = containerCommunicator;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    @Override
    public String toString() {
        return "CommunicateInformation{" +
                "deploymentId='" + deploymentId + '\'' +
                ", containerCommunicator=" + containerCommunicator +
                ", taskNum=" + taskNum +
                '}';
    }
}
