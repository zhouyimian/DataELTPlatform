package com.km.service.PermissionModule.Mapper;

import com.km.service.PermissionModule.domain.AuthorizeNotice;
import com.km.service.PermissionModule.domain.Permission;
import com.km.service.UserModule.domain.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface PermissionMapper {
    @Insert({"insert into permission values(#{userId},#{deploymentId}) "})
    void authorize(@Param("userId") String userId, @Param("deploymentId") String deploymentId);

    @Delete({"delete from permission where userId = #{userId} and deploymentId = #{deploymentId}"})
    void cancelAuthorize(@Param("userId") String userId, @Param("deploymentId") String deploymentId);

    @Insert({"insert into authorizenotice values(#{notice.noticeId},#{notice.senderId},#{notice.senderName}," +
            "#{notice.receiverId},#{notice.receiverName},#{notice.deploymentId},#{notice.deploymentName},#{notice.noticeType}," +
            "#{notice.content},#{notice.sendTime},#{notice.reply},#{notice.deal})"})
    void sendNotice(@Param("notice") AuthorizeNotice notice);

    @Select("select * from authorizenotice where noticeId = #{noticeId}")
    AuthorizeNotice getNoticeByNoticeId(@Param("noticeId")String noticeId);

    @Update({"update authorizenotice set reply = #{notice.reply},deal = #{notice.deal} where noticeId = #{notice.noticeId}"})
    void updateNotice(@Param("notice") AuthorizeNotice notice);

    @Select("select * from permission where userId = #{userId} AND deploymentId = #{deploymentId}")
    Permission checkPermission(@Param("userId")String userId, @Param("deploymentId")String deploymentId);

    @Select({"SELECT * FROM authorizenotice notice WHERE receiverId = #{userId} ORDER BY sendTime DESC LIMIT #{start},#{count} "})
    List<AuthorizeNotice> getPagePrivateAuthorizeNotices(@Param("start")int start, @Param("count")int pageSize, @Param("userId")String userId);


    @Select({"select count(*) from authorizenotice where receiverId = #{userId} "})
    int getPrivateNoticeCount(@Param("userId") String userId);

    @Select({"select count(*) from authorizenotice where noticeId = #{noticeId} "})
    AuthorizeNotice getAuthorizeByNoticeId(@Param("noticeId") String noticeId);

    @Select({"select * from authorizenotice where senderId = #{senderId} AND deploymentId = #{deploymentId} AND deal = 'false' order by sendTime DESC limit 1"})
    AuthorizeNotice getNotDealNotice(@Param("senderId")String senderId, @Param("deploymentId")String deploymentId);

    @Select({"SELECT * FROM authorizenotice notice WHERE receiverId = #{userId} ORDER BY sendTime DESC"})
    List<AuthorizeNotice> getAllPrivateAuthorizeNotices(@Param("userId") String userId);

    @Delete("delete from authorizenotice where noticeId = #{noticeId}")
    void deleteNotice(@Param("noticeId") String noticeId);

    @Select({"select * from user WHERE userId in (select userId from permission WHERE deploymentId = #{deploymentId})"})
    List<User> getAllDeploymentPermissionUser(@Param("deploymentId")String deploymentId);
}