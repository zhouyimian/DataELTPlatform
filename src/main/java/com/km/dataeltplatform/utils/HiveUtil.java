package com.km.dataeltplatform.utils;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HiveUtil {
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Class.forName(driverName);
        descTable("user");
    }

    public static List<String> descTable(String tableName) throws SQLException, ClassNotFoundException {
        Class.forName(driverName);
        Connection con = DriverManager.getConnection("jdbc:hive2://192.168.43.51:10000/db_hive", "root", "313976009");
        Statement stmt = con.createStatement();
        String sql = "desc "+tableName;
        ResultSet rs = stmt.executeQuery(sql);
        List<String> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
    }
    public void createTable() throws ClassNotFoundException, SQLException {
        String sql = "create table user(\n" +
                "id int,\n" +
                "name string,\n" +
                "email string,\n" +
                "phone_number string,\n" +
                "password string,\n" +
                "status int,\n" +
                "create_time DATE,\n" +
                "last_login_time DATE,\n" +
                "last_update_time DATE,\n"+
                "avatar String\n"+
                ")\n" +
                "row format delimited fields terminated by '\\t'";
        Class.forName(driverName);
        Connection con = DriverManager.getConnection("jdbc:hive2://192.168.43.51:10000/db_hive", "root", "313976009");
        Statement stmt = con.createStatement();
        stmt.execute(sql);

        System.out.println("table user created successfully.");

        con.close();
    }
    public static void loadData(String pathName,HttpServletRequest req) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:hive2://192.168.43.51:10000/db_hive", "root", "313976009");
        Statement stmt = con.createStatement();
        String hiveTable = req.getParameter("hiveTable");
        String sql = "load data inpath '"+pathName+"' overwrite into table "+hiveTable;
        stmt.execute(sql);
    }
}
