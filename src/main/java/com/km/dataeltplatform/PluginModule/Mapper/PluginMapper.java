package com.km.dataeltplatform.PluginModule.Mapper;

import com.km.dataeltplatform.PluginModule.domain.Plugin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface PluginMapper {
    @Select({"select * from plugin where Pluginid = #{Pluginid}"})
    public Plugin getPlugByPlugid(@Param("Pluginid") String pluginid);

    @Insert({"insert into plugin values(#{Pluginid},#{Name},#{Paramters},#{Jar},#{ClassPath}) "})
    public void addPlugin(@Param("Pluginid") String pluginid,@Param("Name") String name,
                          @Param("Paramters") String paramters,@Param("Jar") String jar,
                          @Param("ClassPath") String classpath);


}
