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

    @Select({"SELECT p.processid,p.processname,u.username,p.processcontent,p.state,p.updatetime FROM PROCESS p,USER u WHERE p.userid = u.userid ORDER BY updatetime asc limit #{start},#{count} "})
    public List<ProcessUseridDto> findAllProcess(@Param("start") int start, @Param("count") int count);


    @Insert({"insert into  process values (#{process.processId},#{process.processName}," +
            "#{process.userId},#{process.processContent},#{process.state},#{process.updateTime})"})
    public void addProcess(@Param("process")Process process);


    @Update({"update Process set processname=#{process.processName}," +
            "processContent=#{process.processContent},state=#{process.state}," +
            "updatetime=#{process.updateTime}" +
            "where processId = #{process.processId}"})
    public void updateProcess(@Param("process") Process process);

    @Delete({"delete from process where processId = #{processId}"})
    public void deleteProcess(String processId);

    @Select({"select count(*) from process"})
    public int processCount();

}
