package com.km.dataeltplatform.UserModule.Mapper;

import com.km.dataeltplatform.UserModule.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface UserMapper {
    @Select({"select * from user where username = #{username} and password = #{password} "})
    public User login(@Param("username") String username, @Param("password") String password);
}
