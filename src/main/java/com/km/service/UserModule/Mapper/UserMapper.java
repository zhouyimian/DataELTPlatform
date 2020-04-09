package com.km.service.UserModule.Mapper;

import com.km.service.UserModule.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface UserMapper {
    @Select({"select * from user where Username = #{username} and Password = #{password} "})
    User login(@Param("username") String username, @Param("password") String password);

    @Insert({"insert into user values(#{userId},#{username},#{password},#{nickname})"})
    void register(@Param("userId") String userid, @Param("username") String username, @Param("password") String password, @Param("nickname") String nickname);

    @Select({"select * from user where username = #{username}"})
    User findUserByUserName(@Param("username") String username);

    @Select({"select * from user where userId = #{userId}"})
    User getUserByUserId(@Param("userId") String userId);
}
