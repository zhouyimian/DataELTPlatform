package com.km.service.PermissionModule.domain;

public class Permission {
    private String userId;
    private String otherId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOtherId() {
        return otherId;
    }

    public void setOtherId(String otherId) {
        this.otherId = otherId;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "userId='" + userId + '\'' +
                ", otherId='" + otherId + '\'' +
                '}';
    }
}
