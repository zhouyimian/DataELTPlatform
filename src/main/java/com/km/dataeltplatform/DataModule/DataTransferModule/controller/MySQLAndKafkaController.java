package com.km.dataeltplatform.DataModule.DataTransferModule.controller;

import com.alibaba.fastjson.JSONObject;
import com.km.dataeltplatform.DataModule.DataTransferModule.service.JDBCService;
import com.km.dataeltplatform.DataModule.DataTransferModule.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class MySQLAndKafkaController {
    @Autowired
    JDBCService jdbcService;

    @Autowired
    KafkaService kafkaService;

    /**
     * 将Mysql的数据导入到kafka中
     * @param req
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @RequestMapping(value = "/transferMySQL2Kafka", method = RequestMethod.GET)
    public String transferMySQL2Kafka(HttpServletRequest req) throws Exception {
        //从数据库读取要导入表的数据
        List<Map<String,String>> mysqlData = jdbcService.getMysqlData(req);

        List<JSONObject> mysqlDataJsonObject = new ArrayList<>();
        for(Map map:mysqlData){
            mysqlDataJsonObject.add(new JSONObject(map));
        }
        kafkaService.insertDataToKafka(mysqlDataJsonObject,req);

        return mysqlDataJsonObject.get(0).toString();
    }
}
