package com.km.dataeltplatform.DataTransferModule.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;

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
