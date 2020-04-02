package com.km.service.DeploymentModule.domain;

import java.util.Date;

public class Deployment {
    private String deploymentId;
    private String deploymentName;
    private String userId;
    private String sourceConfigureId;
    private String targetConfigureId;
    private String processId;
    private String state;
    private Date updateTime;

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSourceConfigureId() {
        return sourceConfigureId;
    }

    public void setSourceConfigureId(String sourceConfigureId) {
        this.sourceConfigureId = sourceConfigureId;
    }

    public String getTargetConfigureId() {
        return targetConfigureId;
    }

    public void setTargetConfigureId(String targetConfigureId) {
        this.targetConfigureId = targetConfigureId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Deployment{" +
                "deploymentId='" + deploymentId + '\'' +
                ", deploymentName='" + deploymentName + '\'' +
                ", userId='" + userId + '\'' +
                ", sourceConfigureId='" + sourceConfigureId + '\'' +
                ", targetConfigureId='" + targetConfigureId + '\'' +
                ", processId='" + processId + '\'' +
                ", state='" + state + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
