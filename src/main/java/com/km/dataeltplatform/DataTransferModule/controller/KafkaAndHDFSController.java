package com.km.dataeltplatform.DataTransferModule.controller;

import com.km.dataeltplatform.DataTransferModule.service.HDFSService;
import com.km.dataeltplatform.DataTransferModule.service.HiveService;
import com.km.dataeltplatform.DataTransferModule.service.JDBCService;
import com.km.dataeltplatform.DataTransferModule.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;

@RestController
public class KafkaAndHDFSController {
    @Autowired
    JDBCService jdbcService;

    @Autowired
    HiveService hiveService;

    @Autowired
    HDFSService hdfsService;

    @Autowired
    KafkaService kafkaService;

    /**
     * 将kafka的数据导入到HDFS中
     * @param req
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @RequestMapping(value = "/transferKafka2HDFS", method = RequestMethod.GET)
    public void transferKafka2HDFS(HttpServletRequest req) throws IOException {
        kafkaService.insertDataToHDFS(req);
    }
}
