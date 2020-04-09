package com.km.service.JobModule.controller;

import com.alibaba.fastjson.JSONObject;
import com.km.service.JobModule.domain.JobReport;
import com.km.service.JobModule.service.DataService;
import com.km.service.UserModule.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class JobController {

    @Autowired
    private DataService dataService;

    @RequestMapping(value = "/getOneJobReport", method = RequestMethod.POST)
    public Object getOneDeployment(HttpServletRequest req) {
        String jobReportId = req.getParameter("jobReportId");
        JobReport jobReport = dataService.getJobReportByJobReportId(jobReportId);
        return JSONObject.toJSON(jobReport);
    }


    @RequestMapping(value = "/getAllJobReports", method = RequestMethod.POST)
    public Object getAllJobReports(HttpServletRequest req) {
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));
        int pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
        List<JobReport> jobRepotDesc = dataService.getAllJobReports(pageSize, pageNumber);
        int totalSize = dataService.getJobReportCount();
        int totalPages = totalSize / pageSize + (totalSize % pageSize == 0 ? 0 : 1);
        JSONObject message = new JSONObject();
        message.put("pageSize", pageSize);
        message.put("pageNumber", pageNumber);
        message.put("totalPages", totalPages);
        message.put("jobReportDesc", jobRepotDesc);
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/getAllPrivateJobReports", method = RequestMethod.POST)
    public Object getAllPrivateJobReports(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        JSONObject message = new JSONObject();
        List<JobReport> jobReportDesc;
        int pageSize;
        int pageNumber;
        int totalPages;
        if (req.getParameter("pageSize") == null && req.getParameter("pageNumber") == null) {
            jobReportDesc = dataService.getAllPrivateJobReports(user.getUserId());
            pageSize = jobReportDesc.size();
            pageNumber = 1;
            totalPages = 1;
        } else {
            pageSize = Integer.parseInt(req.getParameter("pageSize"));
            pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
            jobReportDesc = dataService.getPagePrivateJobReports(user.getUserId(), pageSize, pageNumber);
            int totalSize = dataService.getPrivateJobReportCount(user.getUserId());
            totalPages = totalSize / pageSize + (totalSize % pageSize == 0 ? 0 : 1);
        }
        message.put("pageSize", pageSize);
        message.put("pageNumber", pageNumber);
        message.put("totalPages", totalPages);
        message.put("jobReportDesc", jobReportDesc);
        return JSONObject.toJSON(message);
    }

}
