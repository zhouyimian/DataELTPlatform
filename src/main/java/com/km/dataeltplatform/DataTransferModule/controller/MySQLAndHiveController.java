package com.km.dataeltplatform.DataTransferModule.controller;

import com.km.dataeltplatform.DataTransferModule.service.HDFSService;
import com.km.dataeltplatform.DataTransferModule.service.HiveService;
import com.km.dataeltplatform.DataTransferModule.service.JDBCService;
import com.km.dataeltplatform.DataTransferModule.utils.DataUtil;
import com.km.dataeltplatform.DataTransferModule.utils.HiveUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
public class MySQLAndHiveController {
    @Autowired
    JDBCService jdbcService;

    @Autowired
    HiveService hiveService;

    @Autowired
    HDFSService hdfsService;

    /**
     * 将Mysql的数据导入到hive中
     * @param req
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @RequestMapping(value = "/transferMySQL2Hive", method = RequestMethod.GET)
    public String transferMySQL2Hive(HttpServletRequest req) throws Exception {
        //从数据库读取要导入表的数据
        List<Map<String,String>> mysqlData = jdbcService.getMysqlData(req);
        //获取Hive表结构
        List<String> tableStruct = HiveUtil.descTable(req.getParameter("hiveTable"));
        //将数据写入HDFS
        String path = hdfsService.insertDataToHDFSWithStruct(mysqlData,req,tableStruct);
        //将HDFS文件导入到hive中
        HiveUtil.loadData(path,req);

        return mysqlData.get(0).toString();
    }
    /**
     * 将Mysql的数据导入到hdfs中
     * @param req
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @RequestMapping(value = "/transferMySQL2HDFS", method = RequestMethod.GET)
    public String transferMySQL2HDFS(HttpServletRequest req) throws Exception {
        //从数据库读取要导入表的数据
        List<Map<String,String>> mysqlData = jdbcService.getMysqlData(req);
        //获取MySQL表结构
        List<String> tableStruct = DataUtil.getMySQLDataStruct(mysqlData);
        //将数据写入HDFS
        hdfsService.insertDataToHDFSWithStruct(mysqlData,req,tableStruct);
        return mysqlData.get(0).toString();
    }
}
