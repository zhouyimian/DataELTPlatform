package com.km.service.DataModule.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.data.common.util.Configuration;
import com.km.data.core.Engine;
import com.km.data.core.statistics.container.communicator.AbstractContainerCommunicator;
import com.km.data.etl.etlUtil.Key;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.common.CommunicateInformation;
import com.km.utils.FileUtil;
import org.springframework.stereotype.Service;

@Service
public class DataService {

    public void startProcess(Conf sourceConf, Conf targetConf, Process process, CommunicateInformation information) {
        Configuration jobConfiguration = buildJobConf(sourceConf,targetConf,process);

        String corePath = "src/main/resources/static/core.json";
        JSONObject corejson = JSONObject.parseObject(FileUtil.readFile(corePath));
        JSONObject jobjson = JSONObject.parseObject(jobConfiguration.toJSON());

        JSONObject mergeConfig = (JSONObject) corejson.clone();
        mergeConfig.putAll(jobjson);


        Engine engine = new Engine();
        engine.start(new Configuration(mergeConfig.toJSONString()), information);

    }

    private Configuration buildJobConf(Conf sourceConf, Conf targetConf, Process process) {
        Configuration jobconf = new Configuration("");
        jobconf.set("job.content.reader",sourceConf.getConfigureContent());
        jobconf.set("job.content.writer",targetConf.getConfigureContent());
        JSONArray processNodeArray = JSONArray.parseArray(process.getProcessContent());
        if(processNodeArray.size()==2){
            jobconf.set("job.content.ETL", "");
        }else{
            JSONArray etlArray = new JSONArray();
            for(int i = 1;i<processNodeArray.size()-1;i++){
                JSONObject node = processNodeArray.getJSONObject(i);
                JSONObject etlNode = new JSONObject();
                etlNode.put(Key.PLUGIN_NAME,node.getString(Key.PLUGIN_NAME));
                etlNode.put(Key.PLUGIN_PARAMETER,node.getString(Key.PLUGIN_PARAMETER));
                etlArray.add(etlNode);
            }
            jobconf.set("job.content.ETL", etlArray);
        }
        return jobconf;
    }
}
