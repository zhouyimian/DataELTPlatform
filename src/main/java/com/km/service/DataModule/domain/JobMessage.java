package com.km.service.DataModule.domain;

import com.km.data.core.enums.State;

public class JobMessage {

    private String jobMessageId;

    private String sourceConfigureContent;
    private String targetConfigureContent;
    private String processContent;
    private String userName;

    private long startTime;
    private long endTime;
    private long recordTime;
    private State state;
    private Throwable throwable;
    private int taskNum;
    private long finishTasks;
    private long readSucceedRecords;
    private long readSucceedBytes;
    private long writeSucceedRecords;
    private long writeSucceedBytes;
    private long totalETLRecords;
    //读数据的字节速度
    private long byteSpeed;
    private long recordSpeed;


    public String getJobMessageId() {
        return jobMessageId;
    }

    public void setJobMessageId(String jobMessageId) {
        this.jobMessageId = jobMessageId;
    }

    public String getSourceConfigureContent() {
        return sourceConfigureContent;
    }

    public void setSourceConfigureContent(String sourceConfigureContent) {
        this.sourceConfigureContent = sourceConfigureContent;
    }

    public String getTargetConfigureContent() {
        return targetConfigureContent;
    }

    public void setTargetConfigureContent(String targetConfigureContent) {
        this.targetConfigureContent = targetConfigureContent;
    }

    public String getProcessContent() {
        return processContent;
    }

    public void setProcessContent(String processContent) {
        this.processContent = processContent;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public long getFinishTasks() {
        return finishTasks;
    }

    public void setFinishTasks(long finishTasks) {
        this.finishTasks = finishTasks;
    }

    public long getReadSucceedRecords() {
        return readSucceedRecords;
    }

    public void setReadSucceedRecords(long readSucceedRecords) {
        this.readSucceedRecords = readSucceedRecords;
    }

    public long getReadSucceedBytes() {
        return readSucceedBytes;
    }

    public void setReadSucceedBytes(long readSucceedBytes) {
        this.readSucceedBytes = readSucceedBytes;
    }

    public long getWriteSucceedRecords() {
        return writeSucceedRecords;
    }

    public void setWriteSucceedRecords(long writeSucceedRecords) {
        this.writeSucceedRecords = writeSucceedRecords;
    }

    public long getWriteSucceedBytes() {
        return writeSucceedBytes;
    }

    public void setWriteSucceedBytes(long writeSucceedBytes) {
        this.writeSucceedBytes = writeSucceedBytes;
    }

    public long getTotalETLRecords() {
        return totalETLRecords;
    }

    public void setTotalETLRecords(long totalETLRecords) {
        this.totalETLRecords = totalETLRecords;
    }

    public long getByteSpeed() {
        return byteSpeed;
    }

    public void setByteSpeed(long byteSpeed) {
        this.byteSpeed = byteSpeed;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }

    public long getRecordSpeed() {
        return recordSpeed;
    }

    public void setRecordSpeed(long recordSpeed) {
        this.recordSpeed = recordSpeed;
    }
}
