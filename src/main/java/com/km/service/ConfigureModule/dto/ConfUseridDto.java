package com.km.service.ConfigureModule.dto;

import java.util.Date;

public class ConfUseridDto {
    private String configureId;
    private String configureType;
    private String userName;
    private String state;
    private Date updateTime;
    private String configureName;
    private String configureContent;
    private String configureStruct;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getConfigureStruct() {
        return configureStruct;
    }

    public void setConfigureStruct(String configureStruct) {
        this.configureStruct = configureStruct;
    }

    @Override
    public String toString() {
        return "ConfUseridDto{" +
                "configureId='" + configureId + '\'' +
                ", configureType='" + configureType + '\'' +
                ", userName='" + userName + '\'' +
                ", state='" + state + '\'' +
                ", updateTime=" + updateTime +
                ", configureName='" + configureName + '\'' +
                ", configureContent='" + configureContent + '\'' +
                ", configureStruct='" + configureStruct + '\'' +
                ", runningJobCount=" + runningJobCount +
                '}';
    }
}
