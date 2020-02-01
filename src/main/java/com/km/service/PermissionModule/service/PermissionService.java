package com.km.service.PermissionModule.service;

import com.km.service.PermissionModule.Mapper.PermissionMapper;
import com.km.service.PermissionModule.domain.Permission;
import com.km.service.UserModule.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    @Autowired
    PermissionMapper permissionMapper;

    public void authorize(String userId, String otherId) {
        permissionMapper.authorize(userId,otherId);
    }

    public void cancelAuthorize(String userId, String otherId) {
        permissionMapper.cancelAuthorize(userId,otherId);
    }

}
