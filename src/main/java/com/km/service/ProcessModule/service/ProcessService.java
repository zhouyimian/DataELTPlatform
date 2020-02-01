package com.km.service.ProcessModule.service;


import com.km.service.ProcessModule.Mapper.ProcessMapper;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.dto.ProcessUseridDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<ProcessUseridDto> findAllProcess(int pageSize, int pageNumber) {
        int start = (pageNumber-1)*pageSize;
        return processMapper.findAllProcess(start,pageSize);
    }

    public int getProcessCount() {
        return processMapper.processCount();
    }
}
