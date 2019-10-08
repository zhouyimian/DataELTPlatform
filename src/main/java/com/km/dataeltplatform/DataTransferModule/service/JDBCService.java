package com.km.dataeltplatform.DataTransferModule.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JDBCService {
    public List<Map<String,String>> getMysqlData(HttpServletRequest req) throws ClassNotFoundException, SQLException {
        String driverName = "com.mysql.cj.jdbc.Driver";
        String ip = req.getParameter("ip");
        String port = req.getParameter("port");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String database = req.getParameter("database");
        String table = req.getParameter("table");
        String url = "jdbc:mysql://"+ip+":"+port+"/"+database+"?serverTimezone=GMT";
        Class.forName(driverName);
        Connection conn = DriverManager.getConnection(url, username, password);
        Statement stat = conn.createStatement();
        ResultSet resultSet = stat.executeQuery("select * from "+table);
        List<Map<String,String>> list = coverJsonObjectList(resultSet);
        stat.close();
        conn.close();
        return list;
    }

    private List<Map<String,String>> coverJsonObjectList(ResultSet resultSet) throws SQLException {
        List<Map<String,String>> list = new ArrayList<>();
        ResultSetMetaData md = resultSet.getMetaData();//获取键名
        int columnCount = md.getColumnCount();//获取行的数量
        while (resultSet.next()) {
            Map<String,String> rowData = new HashMap<>();//声明Map
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), resultSet.getObject(i)+"");//获取键名及值
            }
            list.add(rowData);
        }
        return list;
    }
}
