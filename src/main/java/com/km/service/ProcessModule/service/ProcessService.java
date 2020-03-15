package com.km.service.ProcessModule.service;


import com.km.service.ProcessModule.Mapper.ProcessMapper;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.dto.ProcessUseridDto;
import com.km.service.common.exception.serviceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ProcessService {

    @Autowired
    ProcessMapper processMapper;

    public void addProcess(String processName,String processContent,String userId) {
        String state = "停止";
        String processId = UUID.randomUUID().toString().replace("-","");
        Date nowDate = new Date();
        Process process = new Process();
        process.setProcessId(processId);
        process.setProcessName(processName);
        process.setUserId(userId);
        process.setProcessContent(processContent);
        process.setState(state);
        process.setUpdateTime(nowDate);
        processMapper.addProcess(process);
    }

    public void deleteProcess(String processId) {
        Process process = processMapper.getProcessByProcessId(processId);
        if(process.getRunningJobCount()!=0){
            throw new serviceException("目前有正在运行的任务绑定着该流程，无法删除");
        }
        processMapper.deleteProcess(processId);
    }

    public void updateProcess(String processId, String processName, String processContent) {
        Process process = processMapper.getProcessByProcessId(processId);
        process.setProcessName(processName);
        process.setProcessContent(processContent);
        process.setUpdateTime(new Date());
        processMapper.updateProcess(process);
    }

    public Process getProcessByProcessId(String processId) {
        return processMapper.getProcessByProcessId(processId);
    }

    public List<ProcessUseridDto> getAllProcess(int pageSize, int pageNumber) {
        int start = (pageNumber-1)*pageSize;
        return processMapper.getAllProcess(start,pageSize);
    }

    public int getProcessCount() {
        return processMapper.getProcessCount();
    }

    public List<Process> getProcessList(String processIds) {
        List<Process> processes = new ArrayList<>();
        String[] ids = processIds.split("\t");
        for(String id:ids){
            processes.add(processMapper.getProcessByProcessId(id));
        }
        return processes;
    }
}
