package com.km.data.etl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.data.common.util.Configuration;
import com.km.data.core.transport.channel.Channel;
import com.km.data.etl.etlUtil.Key;
import com.km.data.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ETL {

    public static void process(Channel channel, Configuration configuration) throws Exception {

        JSONArray classAndParameters = JSONArray.parseArray(configuration.toJSON());

        for(Object object:classAndParameters){
            JSONObject oneClassAndParameter = (JSONObject) object;
            processRecord(channel,oneClassAndParameter,configuration);
        }
    }

    private static void processRecord(Channel channel, JSONObject oneClassAndParameter,Configuration configuration) throws Exception {
        String classPath = oneClassAndParameter.getString(Key.PLUGIN_CLASSPATH);
        JSONArray parameters = oneClassAndParameter.getJSONArray(Key.PLUGIN_PARAMETER);
        Plugin plugin = (Plugin) Class.forName(classPath).getConstructor(Configuration.class).newInstance(configuration);
        inject(plugin,parameters);
        plugin.process(channel);
    }

    private static void inject(Plugin plugin, JSONArray parameters) throws Exception {
        for(int i = 0;i<parameters.size();i++){
            JSONObject parameter = parameters.getJSONObject(i);
            for(Map.Entry<String,Object> entry:parameter.entrySet()){
                Field field = plugin.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(plugin,entry.getValue().toString());
            }
        }
    }
}
