package com.km.service.ConfigureModule.Mapper;

import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ConfigureModule.dto.ConfUseridDto;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface ConfigureMapper {
    @Select({"select * from conf where configureId = #{configureId}"})
    public Conf getConfigureByconfigureId(@Param("configureId") String configureId);

    @Select({"SELECT c.configureId,c.configureType,c.configureName,u.username,c.configureContent,c.state,c.updateTime,c.runningJobCount FROM conf c,user u WHERE c.userId = u.userId ORDER BY updateTime DESC LIMIT #{start},#{count} "})
    public List<ConfUseridDto> getAllConfigures(@Param("start") int start, @Param("count") int count);


    @Insert({"insert into  conf values (#{conf.configureId},#{conf.configureType},#{conf.configureName}," +
            "#{conf.userId},#{conf.configureContent},#{conf.state},#{conf.updateTime},#{conf.runningJobCount})"})
    public void addConfigure(@Param("conf")Conf conf);


    @Update({"update conf set configureType=#{conf.configureType},configureName=#{conf.configureName}," +
            "configureContent=#{conf.configureContent},state=#{conf.state}," +
            "updateTime=#{conf.updateTime},runningJobCount=#{conf.runningJobCount} " +
            "where configureId = #{conf.configureId}"})
    public void updateConfigure(@Param("conf") Conf conf);

    @Delete({"delete from conf where configureId = #{configureId}"})
    public void deleteConfigure(@Param("configureId")String configureId);

    @Select({"select count(*) from conf"})
    public int getConfigureCount();

    @Select({"select runningJobCount from conf where configureId = #{configureId}"})
    public int getRunningJobCount(@Param("configureId")String configureId);

}
