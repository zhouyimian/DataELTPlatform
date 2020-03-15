package com.km.service.DataModule;

import com.alibaba.fastjson.JSONObject;
import com.km.data.common.util.Configuration;
import com.km.data.core.Engine;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ProcessModule.domain.Process;
import com.km.utils.FileUtil;
import org.springframework.stereotype.Service;

@Service
public class DataService {


    public void startProcess(Conf sourceConf, Conf targetConf, Process process) {
        Configuration configuration = new Configuration("");
        JSONObject sourceObject = JSONObject.parseObject(sourceConf.getConfigureContent());
        JSONObject targetObject = JSONObject.parseObject(targetConf.getConfigureContent());
        JSONObject processObject = JSONObject.parseObject(targetConf.getConfigureContent());
        configuration.set("job.setting.speed.channel",sourceObject.getInteger("channel"));
        configuration.set("job.content.reader",sourceObject.get("reader"));
        configuration.set("job.content.writer",targetObject.get("writer"));
        configuration.set("job.content.ETL",processObject.get(""));

        String corePath = "src/main/resources/static/core.json";
        JSONObject corejson = JSONObject.parseObject(FileUtil.readFile(corePath));
        JSONObject jobjson = JSONObject.parseObject(configuration.toJSON());

        JSONObject mergeConfig = (JSONObject) corejson.clone();
        mergeConfig.putAll(jobjson);


        Engine engine = new Engine();
        engine.start(new Configuration(mergeConfig.toJSONString()));

    }
}
