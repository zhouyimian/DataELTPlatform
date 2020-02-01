package com.km.service.ProcessModule.domain;

import java.util.Date;

public class Process {
    private String processId;
    private String processName;
    private String userId;
    private String processContent;
    private String state;
    private Date updateTime;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProcessContent() {
        return processContent;
    }

    public void setProcessContent(String processContent) {
        this.processContent = processContent;
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
        return "Process{" +
                "processId='" + processId + '\'' +
                ", processName='" + processName + '\'' +
                ", userId='" + userId + '\'' +
                ", processContent='" + processContent + '\'' +
                ", state='" + state + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
