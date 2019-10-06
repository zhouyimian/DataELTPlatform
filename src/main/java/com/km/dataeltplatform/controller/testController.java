package com.km.dataeltplatform.controller;

import com.km.dataeltplatform.service.testJDBC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

@RestController
public class testController {
    @Autowired
    testJDBC test;
    @RequestMapping(value = "/helloworld",method = RequestMethod.GET)
    public String transferMySQL2Hive(HttpServletRequest req) throws SQLException, ClassNotFoundException {
        String ip = req.getParameter("ip");
        String port = req.getParameter("port");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String database = req.getParameter("database");
        String table = req.getParameter("table");
        List<String> list = test.getList(ip,port,username,password,database,table);
        return list.get(0);
    }
}
