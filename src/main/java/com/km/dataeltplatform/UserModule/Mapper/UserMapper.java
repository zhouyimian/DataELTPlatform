package com.km.dataeltplatform.UserModule.Mapper;

import com.km.dataeltplatform.UserModule.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface UserMapper {
    @Select({"select * from user where Username = #{Username} and Password = #{Password} "})
    public User login(@Param("Username") String username, @Param("Password") String password);

    @Insert({"insert into user values(#{Userid},#{Username},#{Password}) "})
    public void register(@Param("Userid") String userid,@Param("Username") String username, @Param("Password") String password);
}
