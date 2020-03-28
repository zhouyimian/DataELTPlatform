package com.km.service.DeploymentModule.Mapper;

import com.km.service.DeploymentModule.domain.Deployment;
import com.km.service.DeploymentModule.dto.DeploymentUseridDto;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface DeploymentMapper {

    @Select({"select * from deployment where deploymentId = #{deploymentId}"})
    public Deployment getDeploymentBydeployId(@Param("deploymentId") String deploymentId);

    @Select({"SELECT d.deploymentId,d.deploymentName,u.username,d.sourceConfigureId,d,targetConfigureId,d.processIds,d.state,d.updateTime FROM deployment d,user u WHERE d.userId = u.userId ORDER BY updateTime DESC LIMIT #{start},#{count} "})
    public List<DeploymentUseridDto> getAllDeployments(@Param("start") int start, @Param("count") int count);


    @Insert({"insert into deployment values (#{Deployment.deploymentId},#{Deployment.deploymentName}," +
            "#{Deployment.userId},#{Deployment.sourceConfigureId},#{Deployment.targetConfigureId}," +
            "#{Deployment.processIds},#{Deployment.state},#{Deployment.updateTime})"})
    public void addDeployment(@Param("Deployment") Deployment deployment);


    @Update({"update deployment set deploymentName=#{Deployment.deploymentName}," +
            "configureId=#{Deployment.sourceConfigureId},configureId=#{Deployment.targetConfigureId}," +
            "processIds=#{Deployment.processIds},state=#{Deployment.state}," +
            "updateTime=#{Deployment.updateTime} where deploymentId = #{Deployment.deploymentId}"})
    public void updateDeployment(@Param("Deployment") Deployment deployment);

    @Delete({"delete from deployment where deploymentId = #{deploymentId}"})
    public void deleteDeployment(String deploymentId);

    @Select({"select count(*) from deployment"})
    public int getDeploymentCount();

}
