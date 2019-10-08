package com.km.dataeltplatform.DataTransferModule.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class DataUtil {
    public static String coverJsonObject2String(JSONObject jsonObject){
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
    public static List<String> coverJsonObejctList2StringList(List<JSONObject> jsonObjectList){
        List<String> result = new ArrayList<>();
        for(JSONObject object:jsonObjectList){
            result.add(coverJsonObject2String(object));
        }
        return result;
    }

    public static void main(String[] args) {
        Map map = new HashMap<>();
        map.put("key1","value1");
        map.put("key2","value2");
        JSONObject jsonObject = new JSONObject(map);
        System.out.println(jsonObject);
    }

    public static List<String> getMySQLDataStruct(List<Map<String, String>> mysqlData) {
        List<String> dataStruct = new ArrayList<>();
        if(mysqlData==null||mysqlData.size()==0)
            throw new NullPointerException("数据库内容为空");
        Map<String,String> struct = mysqlData.get(0);
        for(Map.Entry<String,String> entry:struct.entrySet())
            dataStruct.add(entry.getKey());
        return dataStruct;
    }
}
