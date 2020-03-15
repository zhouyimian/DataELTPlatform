package com.km.service.DeploymentModule.dto;

import java.util.Date;

public class DeploymentUseridDto {
    private String deploymentId;
    private String deploymentName;
    private String userName;
    private String sourceConfigureId;
    private String targetConfigureId;
    private String processIds;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getProcessIds() {
        return processIds;
    }

    public void setProcessIds(String processIds) {
        this.processIds = processIds;
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
        return "DeploymentUseridDto{" +
                "deploymentId='" + deploymentId + '\'' +
                ", deploymentName='" + deploymentName + '\'' +
                ", userName='" + userName + '\'' +
                ", sourceConfigureId='" + sourceConfigureId + '\'' +
                ", targetConfigureId='" + targetConfigureId + '\'' +
                ", processIds='" + processIds + '\'' +
                ", state='" + state + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
