package com.km.dataeltplatform.PluginModule.domain;

public class Plugin {
    private String Pluginid;
    private String Name;
    private String Paramters;
    private String Jar;
    private String ClassPath;

    public String getPluginid() {
        return Pluginid;
    }

    public void setPluginid(String pluginid) {
        Pluginid = pluginid;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getParamters() {
        return Paramters;
    }

    public void setParamters(String paramters) {
        Paramters = paramters;
    }

    public String getJar() {
        return Jar;
    }

    public void setJar(String jar) {
        Jar = jar;
    }

    public String getClassPath() {
        return ClassPath;
    }

    public void setClassPath(String classPath) {
        ClassPath = classPath;
    }

    @Override
    public String toString() {
        return "plugin{" +
                "Pluginid='" + Pluginid + '\'' +
                ", Name='" + Name + '\'' +
                ", Paramters='" + Paramters + '\'' +
                ", Jar='" + Jar + '\'' +
                ", ClassPath='" + ClassPath + '\'' +
                '}';
    }
}
