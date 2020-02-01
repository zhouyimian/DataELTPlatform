package com.km.service.PermissionModule.Mapper;

import com.km.service.PermissionModule.domain.Permission;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface PermissionMapper {
    @Insert({"insert into permission values(#{userId},#{otherId}) "})
    public void authorize(@Param("userId") String userId, @Param("otherId") String otherId);

    @Delete({"delete from permission where userId = #{userId} and otherId = #{otherId}"})
    public void cancelAuthorize(@Param("userId") String userId, @Param("otherId") String otherId);

}
