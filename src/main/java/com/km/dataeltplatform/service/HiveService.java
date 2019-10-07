package com.km.dataeltplatform.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HiveService {


    public void loadHDFSData(String path,HttpServletRequest req) throws ClassNotFoundException, SQLException {
        String driverName = "org.apache.hive.jdbc.HiveDriver";
        Class.forName(driverName);
        Connection con = DriverManager.getConnection("jdbc:hive2://192.168.43.51:10000/default", "root", "313976009");
        Statement stmt = con.createStatement();

        stmt.close();
        con.close();

    }

}
