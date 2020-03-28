package com.km.data;

import com.alibaba.fastjson.JSONObject;
import com.km.data.common.util.Configuration;
import com.km.data.core.Engine;
import com.km.utils.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class testStart {


    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        System.setProperty("hadoop.home.dir", "F:\\github\\winutils-master\\hadoop-2.6.0");
        String corePath = "src/main/resources/static/core.json";
        String jobPath = "src/main/resources/static/job/job.json";

        JSONObject corejson = JSONObject.parseObject(FileUtil.readFile(corePath));
        JSONObject jobjson = JSONObject.parseObject(FileUtil.readFile(jobPath));

        JSONObject mergeConfig = (JSONObject) corejson.clone();
        mergeConfig.putAll(jobjson);


        Engine engine = new Engine();
        engine.start(new Configuration(mergeConfig.toJSONString()));

        HashMap<String,String> map = new HashMap<>();

    }


}
