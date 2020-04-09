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
    Deployment getDeploymentBydeployId(@Param("deploymentId") String deploymentId);

    @Select({"SELECT d.deploymentId,d.deploymentName,u.username,d.sourceConfigureId,d.targetConfigureId,d.processId,d.state,d.updateTime FROM deployment d,user u WHERE d.userId = u.userId ORDER BY updateTime DESC LIMIT #{start},#{count} "})
    List<DeploymentUseridDto> getAllDeployments(@Param("start") int start, @Param("count") int count);


    @Insert({"insert into deployment values (#{Deployment.deploymentId},#{Deployment.deploymentName}," +
            "#{Deployment.userId},#{Deployment.sourceConfigureId},#{Deployment.targetConfigureId}," +
            "#{Deployment.processId},#{Deployment.state},#{Deployment.updateTime})"})
    void addDeployment(@Param("Deployment") Deployment deployment);


    @Update({"update deployment set deploymentName=#{Deployment.deploymentName}," +
            "sourceConfigureId=#{Deployment.sourceConfigureId},targetConfigureId=#{Deployment.targetConfigureId}," +
            "processId=#{Deployment.processId},state=#{Deployment.state}," +
            "updateTime=#{Deployment.updateTime} where deploymentId = #{Deployment.deploymentId}"})
    void updateDeployment(@Param("Deployment") Deployment deployment);

    @Delete({"delete from deployment where deploymentId = #{deploymentId}"})
    void deleteDeployment(@Param("deploymentId") String deploymentId);

    @Select({"select count(*) from deployment"})
    int getDeploymentCount();

    @Select({"SELECT d.deploymentId,d.deploymentName,u.username,d.sourceConfigureId,d.targetConfigureId,d.processId,d.state,d.updateTime " +
            "FROM deployment d,user u " +
            "WHERE d.userId =#{userId} AND d.userId = u.userId ORDER BY updateTime DESC"})
    List<DeploymentUseridDto> getAllPrivateDeployments(@Param("userId")String userId);

    @Select({"SELECT d.deploymentId,d.deploymentName,u.username,d.sourceConfigureId,d.targetConfigureId,d.processId,d.state,d.updateTime " +
            "FROM deployment d,USER u,permission p " +
            "WHERE u.userId = #{userId} AND d.deploymentId = p.deploymentId AND p.userId = #{userId} ORDER BY updateTime DESC"})
    List<DeploymentUseridDto> getAllPermissionDeployments(@Param("userId")String userId);

    @Select({"select count(*) from deployment where userId = #{userId}"})
    int getPrivateDeploymentCount(@Param("userId")String userId);

    @Select({"SELECT d.deploymentId,d.deploymentName,u.username,d.sourceConfigureId,d.targetConfigureId,d.processId,d.state,d.updateTime " +
            "FROM deployment d,user u " +
            "WHERE u.userId = #{userId} AND d.userId = u.userId ORDER BY updateTime DESC LIMIT #{start},#{count} "})
    List<DeploymentUseridDto> getPagePrivateDeployments(@Param("userId")String userId,@Param("start") int start, @Param("count") int count);

    @Select({"SELECT d.deploymentId,d.deploymentName,u.username,d.sourceConfigureId,d.targetConfigureId,d.processId,d.state,d.updateTime " +
            "FROM deployment d,user u " +
            "WHERE d.userId = u.userId AND d.deploymentId IN (" +
            "SELECT d.deploymentId  FROM deployment d,permission p WHERE d.deploymentId = p.deploymentId AND p.userId = #{userId}) ORDER BY updateTime DESC "})
    List<DeploymentUseridDto> getUserAuthorizedDeployments(@Param("userId")String userId);
}
