package com.km.service.UserModule.domain;

public class User {
    private String Userid;
    private String Username;
    private String Password;

    @Override
    public String toString() {
        return "User{" +
                "Userid='" + Userid + '\'' +
                ", Username='" + Username + '\'' +
                ", Password='" + Password + '\'' +
                '}';
    }

    public String getUserid() {
        return Userid;
    }

    public void setUserid(String userid) {
        Userid = userid;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
