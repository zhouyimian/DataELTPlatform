package com.km.service.DataModule.Mapper;

import com.km.service.DataModule.domain.JobMessage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface JobMessageMapper {

    void addMessage(JobMessage jobMessage);
}
