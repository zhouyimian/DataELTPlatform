package com.km.service.ConfigureModule.domain;

import java.util.Date;

public class Conf {
    private String configureId;
    private String configureType;
    private String configureName;
    private String userId;
    private String state;
    private Date updateTime;
    private String configureContent;
    private int runningJobCount;

    public String getConfigureType() {
        return configureType;
    }

    public void setConfigureType(String configureType) {
        this.configureType = configureType;
    }

    public int getRunningJobCount() {
        return runningJobCount;
    }

    public void setRunningJobCount(int runningJobCount) {
        this.runningJobCount = runningJobCount;
    }

    public String getConfigureId() {
        return configureId;
    }

    public void setConfigureId(String configureId) {
        this.configureId = configureId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getConfigureName() {
        return configureName;
    }

    public void setConfigureName(String configureName) {
        this.configureName = configureName;
    }

    public String getConfigureContent() {
        return configureContent;
    }

    public void setConfigureContent(String configureContent) {
        this.configureContent = configureContent;
    }

    @Override
    public String toString() {
        return "Conf{" +
                "configureId='" + configureId + '\'' +
                ", configureType='" + configureType + '\'' +
                ", configureName='" + configureName + '\'' +
                ", userId='" + userId + '\'' +
                ", state='" + state + '\'' +
                ", updateTime=" + updateTime +
                ", configureContent='" + configureContent + '\'' +
                ", runningJobCount=" + runningJobCount +
                '}';
    }
}
