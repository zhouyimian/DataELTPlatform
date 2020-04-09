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
    Process getProcessByProcessId(@Param("processId") String processId);

    @Select({"SELECT p.processId,p.processName,u.username,p.processContent,p.state,p.updateTime,p.runningJobCount FROM process p,user u WHERE p.userId = u.userId ORDER BY updateTime DESC limit #{start},#{count} "})
    List<ProcessUseridDto> getAllProcess(@Param("start") int start, @Param("count") int count);


    @Insert({"insert into  process values (#{process.processId},#{process.processName}," +
            "#{process.userId},#{process.processContent},#{process.state},#{process.updateTime},#{process.runningJobCount})"})
    void addProcess(@Param("process") Process process);


    @Update({"update process set processName=#{process.processName}," +
            "processContent=#{process.processContent},state=#{process.state}," +
            "updateTime=#{process.updateTime},runningJobCount=#{process.runningJobCount} " +
            "where processId = #{process.processId}"})
    void updateProcess(@Param("process") Process process);

    @Delete({"delete from process where processId = #{processId}"})
    void deleteProcess(@Param("processId") String processId);

    @Select({"select count(*) from process"})
    int getProcessCount();

    @Select({"SELECT p.processId,p.processName,u.username,p.processContent,p.state,p.updateTime,p.runningJobCount FROM process p,user u WHERE u.userId = #{userId} AND p.userId = u.userId  ORDER BY updateTime DESC"})
    List<ProcessUseridDto> getAllPrivateProcess(@Param("userId") String userId);

    @Select({"SELECT p.processId,p.processName,u.username,p.processContent,p.state,p.updateTime,p.runningJobCount FROM process p,user u WHERE u.userId = #{userId} AND p.userId = u.userId  ORDER BY updateTime DESC limit #{start},#{count}"})
    List<ProcessUseridDto> getPagePrivateProcess(@Param("userId") String userId, @Param("start") int start, @Param("count") int count);

    @Select({"select count(*) from process where userId = #{userId}"})
    int getPrivateProcessCount(@Param("userId")String userId);
}
