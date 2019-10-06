package com.km.dataeltplatform.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class testJDBC {


    public List<String> getList(String ip, String port, String username, String password, String database, String table) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://"+ip+":"+port+"/"+database+"?serverTimezone=GMT";
        Connection conn = DriverManager.getConnection(url, username, password);
        Statement stat = conn.createStatement();
        ResultSet resultSet = stat.executeQuery("select * from "+table);
        List<String> list = coverList(resultSet);
        return list;
    }

    private List<String> coverList(ResultSet resultSet) throws SQLException {
        List<String> list = new ArrayList();
        ResultSetMetaData md = resultSet.getMetaData();//获取键名
        int columnCount = md.getColumnCount();//获取行的数量
        while (resultSet.next()) {
            Map rowData = new HashMap();//声明Map
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), resultSet.getObject(i));//获取键名及值
            }
            list.add(rowData.toString());
        }
        return list;
    }
}
