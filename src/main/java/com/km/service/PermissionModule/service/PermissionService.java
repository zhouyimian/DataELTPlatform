package com.km.service.PermissionModule.service;

import com.km.service.PermissionModule.Mapper.PermissionMapper;
import com.km.service.PermissionModule.domain.AuthorizeNotice;
import com.km.service.PermissionModule.domain.Permission;
import com.km.service.UserModule.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {

    @Autowired
    PermissionMapper permissionMapper;

    public void authorize(String userId, String deploymentId) {
        permissionMapper.authorize(userId,deploymentId);
    }

    public void cancelAuthorize(String userId, String otherId) {
        permissionMapper.cancelAuthorize(userId,otherId);
    }

    public void sendNotice(AuthorizeNotice notice) {
        permissionMapper.sendNotice(notice);
    }

    public AuthorizeNotice getNoticeByNoticeId(String noticeId) {
        return permissionMapper.getNoticeByNoticeId(noticeId);
    }

    public void updateNotice(AuthorizeNotice notice) {
        permissionMapper.updateNotice(notice);
    }

    public boolean checkPermission(String userId, String deploymentId) {
        Permission permission = permissionMapper.checkPermission(userId,deploymentId);
        return permission!=null;
    }

    public List<AuthorizeNotice> getPagePrivateAuthorizeNotices(int pageSize, int pageNumber, String userId) {
        int start = (pageNumber-1)*pageSize;
        return permissionMapper.getPagePrivateAuthorizeNotices(start,pageSize,userId);
    }

    public List<AuthorizeNotice> getAllPrivateAuthorizeNotices(String userId) {
        return permissionMapper.getAllPrivateAuthorizeNotices(userId);
    }

    public int getPrivateNoticeCount(String userId) {
        return permissionMapper.getPrivateNoticeCount(userId);
    }

    public AuthorizeNotice getAuthorizeByNoticeId(String noticeId) {
        return permissionMapper.getAuthorizeByNoticeId(noticeId);
    }

    public AuthorizeNotice getNotDealNotice(String userId, String deploymentId) {
        return permissionMapper.getNotDealNotice(userId,deploymentId);
    }

    public void deleteNotice(String noticeId) {
        permissionMapper.deleteNotice(noticeId);
    }

    public List<User> getAllDeploymentPermissionUser(String deploymentId) {
        return permissionMapper.getAllDeploymentPermissionUser(deploymentId);
    }
}
