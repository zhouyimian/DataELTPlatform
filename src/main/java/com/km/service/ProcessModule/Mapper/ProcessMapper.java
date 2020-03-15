package com.km.service.ProcessModule.Mapper;

import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.dto.ProcessUseridDto;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface ProcessMapper {

    @Select({"select * from process where processId = #{processId}"})
    public Process getProcessByProcessId(@Param("processId") String processId);

    @Select({"SELECT p.processId,p.processName,u.username,p.processContent,p.state,p.updateTime,p.runningJobCount FROM PROCESS p,USER u WHERE p.userId = u.userId ORDER BY updateTime DESC limit #{start},#{count} "})
    public List<ProcessUseridDto> getAllProcess(@Param("start") int start, @Param("count") int count);


    @Insert({"insert into  process values (#{process.processId},#{process.processName}," +
            "#{process.userId},#{process.processContent},#{process.state},#{process.updateTime},#{process.runningJobCount})"})
    public void addProcess(@Param("process")Process process);


    @Update({"update Process set processName=#{process.processName}," +
            "processContent=#{process.processContent},state=#{process.state}," +
            "updateTime=#{process.updateTime},runningJobCount=#{process.runningJobCount} " +
            "where processId = #{process.processId}"})
    public void updateProcess(@Param("process") Process process);

    @Delete({"delete from process where processId = #{processId}"})
    public void deleteProcess(@Param("processId")String processId);

    @Select({"select count(*) from process"})
    public int getProcessCount();

}
