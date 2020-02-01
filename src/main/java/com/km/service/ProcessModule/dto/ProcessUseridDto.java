package com.km.service.ProcessModule.dto;

import java.util.Date;

public class ProcessUseridDto {
    private String processId;
    private String processName;
    private String userName;
    private String processcontent;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProcesscontent() {
        return processcontent;
    }

    public void setProcesscontent(String processcontent) {
        this.processcontent = processcontent;
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
        return "ProcessUseridDto{" +
                "processId='" + processId + '\'' +
                ", processName='" + processName + '\'' +
                ", userName='" + userName + '\'' +
                ", processcontent='" + processcontent + '\'' +
                ", state='" + state + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
