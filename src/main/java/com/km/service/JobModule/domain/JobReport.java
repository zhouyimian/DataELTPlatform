package com.km.service.JobModule.domain;

import java.util.Date;

public class JobReport {

    private String jobReportId;
    private String sourceConfigureContent;
    private String sourceConfigureStruct;
    private String targetConfigureContent;
    private String targetConfigureStruct;
    private String processContent;
    private String deploymentId;
    private String deploymentName;
    private String deploymentContainerId;
    private String deploymentContainerName;
    private String startUserId;
    private String startUserName;

    private String stopUserId;
    private String stopUserName;

    private Date startTime;
    private Date endTime;
    //该报告产生时间
    private Date recordTime;
    private String state;
    private String throwable;
    private int taskNum;
    private int finishTasks;
    private long readSucceedRecords;
    private long readSucceedBytes;
    private long writeSucceedRecords;
    private long writeSucceedBytes;
    private long totalInputEtlRecords;
    private long totalOutputEtlRecords;
    //读数据的字节速度
    private long averageByteSpeed;
    private long averageRecordSpeed;


    public String getJobReportId() {
        return jobReportId;
    }

    public void setJobReportId(String jobReportId) {
        this.jobReportId = jobReportId;
    }

    public String getSourceConfigureContent() {
        return sourceConfigureContent;
    }

    public void setSourceConfigureContent(String sourceConfigureContent) {
        this.sourceConfigureContent = sourceConfigureContent;
    }

    public String getSourceConfigureStruct() {
        return sourceConfigureStruct;
    }

    public void setSourceConfigureStruct(String sourceConfigureStruct) {
        this.sourceConfigureStruct = sourceConfigureStruct;
    }

    public String getTargetConfigureContent() {
        return targetConfigureContent;
    }

    public void setTargetConfigureContent(String targetConfigureContent) {
        this.targetConfigureContent = targetConfigureContent;
    }

    public String getTargetConfigureStruct() {
        return targetConfigureStruct;
    }

    public void setTargetConfigureStruct(String targetConfigureStruct) {
        this.targetConfigureStruct = targetConfigureStruct;
    }

    public String getProcessContent() {
        return processContent;
    }

    public void setProcessContent(String processContent) {
        this.processContent = processContent;
    }

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

    public String getDeploymentContainerId() {
        return deploymentContainerId;
    }

    public void setDeploymentContainerId(String deploymentContainerId) {
        this.deploymentContainerId = deploymentContainerId;
    }

    public String getDeploymentContainerName() {
        return deploymentContainerName;
    }

    public void setDeploymentContainerName(String deploymentContainerName) {
        this.deploymentContainerName = deploymentContainerName;
    }

    public String getStartUserId() {
        return startUserId;
    }

    public void setStartUserId(String startUserId) {
        this.startUserId = startUserId;
    }

    public String getStartUserName() {
        return startUserName;
    }

    public void setStartUserName(String startUserName) {
        this.startUserName = startUserName;
    }

    public String getStopUserId() {
        return stopUserId;
    }

    public void setStopUserId(String stopUserId) {
        this.stopUserId = stopUserId;
    }

    public String getStopUserName() {
        return stopUserName;
    }

    public void setStopUserName(String stopUserName) {
        this.stopUserName = stopUserName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getThrowable() {
        return throwable;
    }

    public void setThrowable(String throwable) {
        this.throwable = throwable;
    }

    public int getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(int taskNum) {
        this.taskNum = taskNum;
    }

    public int getFinishTasks() {
        return finishTasks;
    }

    public void setFinishTasks(int finishTasks) {
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

    public long getTotalInputEtlRecords() {
        return totalInputEtlRecords;
    }

    public void setTotalInputEtlRecords(long totalInputEtlRecords) {
        this.totalInputEtlRecords = totalInputEtlRecords;
    }

    public long getTotalOutputEtlRecords() {
        return totalOutputEtlRecords;
    }

    public void setTotalOutputEtlRecords(long totalOutputEtlRecords) {
        this.totalOutputEtlRecords = totalOutputEtlRecords;
    }

    public long getAverageByteSpeed() {
        return averageByteSpeed;
    }

    public void setAverageByteSpeed(long averageByteSpeed) {
        this.averageByteSpeed = averageByteSpeed;
    }

    public long getAverageRecordSpeed() {
        return averageRecordSpeed;
    }

    public void setAverageRecordSpeed(long averageRecordSpeed) {
        this.averageRecordSpeed = averageRecordSpeed;
    }
}
