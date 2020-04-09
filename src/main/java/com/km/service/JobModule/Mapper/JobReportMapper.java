package com.km.service.JobModule.Mapper;

import com.km.service.JobModule.domain.JobReport;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface JobReportMapper {
    @Insert("insert into jobreport values(#{jobReport.jobReportId},#{jobReport.sourceConfigureContent}," +
            "#{jobReport.sourceConfigureStruct},#{jobReport.targetConfigureContent}," +
            "#{jobReport.targetConfigureStruct},#{jobReport.processContent},#{jobReport.deploymentId}," +
            "#{jobReport.deploymentName},#{jobReport.deploymentContainerId}" +
            ",#{jobReport.deploymentContainerName},#{jobReport.startUserId},#{jobReport.startUserName}," +
            "#{jobReport.stopUserId},#{jobReport.stopUserName},#{jobReport.startTime}," +
            "#{jobReport.endTime},#{jobReport.recordTime},#{jobReport.state}," +
            "#{jobReport.throwable},#{jobReport.taskNum},#{jobReport.finishTasks}," +
            "#{jobReport.readSucceedRecords},#{jobReport.readSucceedBytes},#{jobReport.writeSucceedRecords}," +
            "#{jobReport.writeSucceedBytes},#{jobReport.totalInputEtlRecords},#{jobReport.totalOutputEtlRecords}," +
            "#{jobReport.averageByteSpeed},#{jobReport.averageRecordSpeed})")
    void saveJobReport(@Param("jobReport") JobReport jobReport);

    @Select("select * from jobreport where jobReportId = #{jobReportId}")
    JobReport getJobReportByJobReportId(@Param("jobReportId") String jobReportId);


    @Delete("delete from jobreport where jobReportId = #{jobReportId}")
    void deleteJobReport(@Param("jobReportId") String jobReportId);

    @Select("select * from jobreport ORDER BY recordTime DESC limit #{start},#{count}")
    List<JobReport> getAllJobReports(@Param("start") int start, @Param("count") int count);

    @Select("select * from jobreport where startUserId = #{userId} ORDER BY recordTime DESC limit #{start},#{count}")
    List<JobReport> getPagePrivateJobReports(@Param("userId") String userId,@Param("start") int start, @Param("count") int count);

    @Select("select * from jobreport where startUserId = #{userId} ORDER BY recordTime DESC")
    List<JobReport> getAllPrivateJobReports(@Param("userId") String userId);

    @Select("select count(*) from jobreport")
    int getJobReportCount();

    @Select("select count(*) from jobreport where startUserId = #{userId}")
    int getPrivateJobReportCount(@Param("userId")String userId);
}
