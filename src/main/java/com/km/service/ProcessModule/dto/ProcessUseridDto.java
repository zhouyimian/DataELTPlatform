package com.km.service.ProcessModule.dto;

import java.util.Date;

public class ProcessUseridDto {
    private String processId;
    private String processName;
    private String userName;
    private String processContent;
    private String state;
    private Date updateTime;
    private int runningJobCount;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public int getRunningJobCount() {
        return runningJobCount;
    }

    public void setRunningJobCount(int runningJobCount) {
        this.runningJobCount = runningJobCount;
    }

    @Override
    public String toString() {
        return "ProcessUseridDto{" +
                "processId='" + processId + '\'' +
                ", processName='" + processName + '\'' +
                ", userName='" + userName + '\'' +
                ", processContent='" + processContent + '\'' +
                ", state='" + state + '\'' +
                ", updateTime=" + updateTime +
                ", runningJobCount=" + runningJobCount +
                '}';
    }
}
