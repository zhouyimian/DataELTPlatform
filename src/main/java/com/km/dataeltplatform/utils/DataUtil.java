package com.km.dataeltplatform.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
