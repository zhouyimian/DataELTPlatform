package com.km.service.PermissionModule.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.km.service.ConfigureModule.domain.Conf;
import com.km.service.ConfigureModule.service.ConfigureService;
import com.km.service.DeploymentModule.domain.Deployment;
import com.km.service.DeploymentModule.service.DeploymentService;
import com.km.service.PermissionModule.domain.AuthorizeNotice;
import com.km.service.PermissionModule.domain.NoticeType;
import com.km.service.PermissionModule.service.PermissionService;
import com.km.service.ProcessModule.domain.Process;
import com.km.service.ProcessModule.service.ProcessService;
import com.km.service.UserModule.domain.User;
import com.km.service.UserModule.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ConfigureService configureService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/authorizeRequest", method = RequestMethod.POST)
    public Object authorizeRequest(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        Deployment deployment = deploymentService.getDeploymentBydeployId(deploymentId);
        User user = (User) req.getAttribute("user");
        JSONObject message = new JSONObject();
        if (deployment.getUserId().equals(user.getUserId())) {
            message.put("message", "该用户是该流程所有者,无需授权");
        } else {
            User receiver = userService.getUserByUserId(deployment.getUserId());
            AuthorizeNotice notice = new AuthorizeNotice();
            notice.setNoticeId(UUID.randomUUID().toString().replace("-", ""));
            notice.setSenderId(user.getUserId());
            notice.setSenderName(user.getUserName());
            notice.setReceiverId(receiver.getUserId());
            notice.setReceiverName(receiver.getUserName());
            notice.setDeploymentId(deployment.getDeploymentId());
            notice.setDeploymentName(deployment.getDeploymentName());
            notice.setNoticeType(NoticeType.authorizeRequest);
            notice.setContent("用户 " + user.getUserName() + "向你请求获取部署 " + deployment.getDeploymentName() + "的使用权限");
            notice.setSendTime(new Date());
            notice.setReply("false");
            notice.setDeal("false");
            permissionService.sendNotice(notice);
            message.put("message", "已向用户 " + receiver.getUserName() + "发出部署授权请求");
        }
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/authorizeAllow", method = RequestMethod.POST)
    public Object authorizeAllow(HttpServletRequest req) {

        String noticeId = req.getParameter("noticeId");
        AuthorizeNotice notice = permissionService.getNoticeByNoticeId(noticeId);
        notice.setDeal("true");
        permissionService.updateNotice(notice);
        AuthorizeNotice replyNotice = BuildReplyNotice(notice);
        replyNotice.setNoticeType(NoticeType.authorizeAllow);
        replyNotice.setContent("用户 " + notice.getReceiverName() + "同意授权给你部署 " + notice.getDeploymentName() + "的使用权限");
        permissionService.sendNotice(replyNotice);
        permissionService.authorize(notice.getSenderId(), notice.getDeploymentId());

        JSONObject message = new JSONObject();
        message.put("message", "已向用户 " + notice.getSenderName() + "发送同意授权消息");
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/authorizeReject", method = RequestMethod.POST)
    public Object authorizeReject(HttpServletRequest req) {
        String noticeId = req.getParameter("noticeId");
        AuthorizeNotice notice = permissionService.getNoticeByNoticeId(noticeId);
        notice.setDeal("true");
        permissionService.updateNotice(notice);

        AuthorizeNotice replyNotice = BuildReplyNotice(notice);
        replyNotice.setNoticeType(NoticeType.authorizeReject);
        replyNotice.setContent("用户 " + notice.getReceiverName() + "拒绝授权给你部署 " + notice.getDeploymentName() + "的使用权限");
        permissionService.sendNotice(replyNotice);

        JSONObject message = new JSONObject();
        message.put("message", "已向用户 " + notice.getSenderName() + "发送拒绝授权消息");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/cancelAuthorize", method = RequestMethod.POST)
    public Object cancelAuthorize(HttpServletRequest req) {
        String userId = req.getParameter("userId");
        String deploymentId = req.getParameter("deploymentId");
        Deployment deployment = deploymentService.getDeploymentBydeployId(deploymentId);
        User receiver = userService.getUserByUserId(userId);
        User user = (User) req.getAttribute("user");

        AuthorizeNotice notice = new AuthorizeNotice();
        notice.setNoticeId(UUID.randomUUID().toString().replace("-", ""));
        notice.setSenderId(user.getUserId());
        notice.setSenderName(user.getUserName());
        notice.setReceiverId(receiver.getUserId());
        notice.setReceiverName(receiver.getUserName());
        notice.setDeploymentId(deployment.getDeploymentId());
        notice.setDeploymentName(deployment.getDeploymentName());
        notice.setNoticeType(NoticeType.cancelAuthorize);
        notice.setContent("用户 " + user.getUserName() + "取消了你对部署 " + deployment.getDeploymentName() + "的使用权限");
        notice.setSendTime(new Date());
        notice.setReply("false");
        permissionService.sendNotice(notice);
        permissionService.cancelAuthorize(userId,deploymentId);

        JSONObject message = new JSONObject();
        message.put("message", "已向取消对用户 " + receiver.getUserName() + "对该部署的授权,并已发出取消部署授权消息");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/deleteNotice", method = RequestMethod.POST)
    public Object deleteNotice(HttpServletRequest req) {
        String noticeId = req.getParameter("noticeId");
        permissionService.deleteNotice(noticeId);
        JSONObject message = new JSONObject();
        message.put("message", "消息已删除");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/batchDeleteNotice", method = RequestMethod.POST)
    public Object batchDeleteNotice(HttpServletRequest req) {
        String ids = req.getParameter("noticeIds");
        JSONArray noticeIds = JSONArray.parseArray(ids);
        JSONObject message = new JSONObject();
        for (int i = 0; i < noticeIds.size(); i++)
            permissionService.deleteNotice(noticeIds.getString(i));
        message.put("message", "消息已清空");
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/checkProcessPermission", method = RequestMethod.POST)
    public Object checkProcessPermission(HttpServletRequest req) {
        String processId = req.getParameter("processId");
        Process process = processService.getProcessByProcessId(processId);
        User user = (User) req.getAttribute("user");

        JSONObject message = new JSONObject();
        if (user.getUserId().equals(process.getUserId()))
            message.put("message", "true");
        else
            message.put("message", "false");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/checkConfigurePermission", method = RequestMethod.POST)
    public Object checkConfigurePermission(HttpServletRequest req) {
        String configureId = req.getParameter("configureId");
        Conf conf = configureService.getConfigureByconfigureId(configureId);
        User user = (User) req.getAttribute("user");
        JSONObject message = new JSONObject();
        if (user.getUserId().equals(conf.getUserId()))
            message.put("message", "true");
        else
            message.put("message", "false");
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/checkDeploymentPermission", method = RequestMethod.POST)
    public Object checkDeploymentPermission(HttpServletRequest req) {
        String deploymentId = req.getParameter("deploymentId");
        Deployment deployment = deploymentService.getDeploymentBydeployId(deploymentId);
        User user = (User) req.getAttribute("user");
        JSONObject message = new JSONObject();
        if (user.getUserId().equals(deployment.getUserId()))
            message.put("message", "true");
        else {
            boolean result = permissionService.checkPermission(user.getUserId(), deploymentId);
            if (result) {
                message.put("message", result + "");
            } else {
                AuthorizeNotice authorizeNotice = permissionService.getNotDealNotice(user.getUserId(), deploymentId);
                if (authorizeNotice == null) {
                    message.put("message", "false");
                } else {
                    message.put("message", "您之前已经发送对该部署的权限请求,请耐心等待部署所有者通过您的申请");
                }
            }
        }
        return JSONObject.toJSON(message);
    }


    @RequestMapping(value = "/getAllPrivateAuthorizeNotices", method = RequestMethod.POST)
    public Object getAllPrivateAuthorizeNotices(HttpServletRequest req) {
        User user = (User) req.getAttribute("user");
        JSONObject message = new JSONObject();
        List<AuthorizeNotice> noticesList;
        int pageSize;
        int pageNumber;
        int totalPages;
        if (req.getParameter("pageSize") == null && req.getParameter("pageNumber") == null) {
            noticesList = permissionService.getAllPrivateAuthorizeNotices(user.getUserId());
            pageSize = noticesList.size();
            pageNumber = 1;
            totalPages = 1;
        } else {
            pageSize = Integer.parseInt(req.getParameter("pageSize"));
            pageNumber = Integer.parseInt(req.getParameter("pageNumber"));
            noticesList = permissionService.getPagePrivateAuthorizeNotices(pageSize, pageNumber, user.getUserId());
            int totalSize = permissionService.getPrivateNoticeCount(user.getUserId());
            totalPages = totalSize / pageSize + (totalSize % pageSize == 0 ? 0 : 1);
        }
        message.put("pageSize", pageSize);
        message.put("pageNumber", pageNumber);
        message.put("totalPages", totalPages);
        message.put("noticesDesc", noticesList);
        return JSONObject.toJSON(message);
    }

    @RequestMapping(value = "/getOnePrivateAuthorizeNotices", method = RequestMethod.POST)
    public Object getOnePrivateAuthorizeNotices(HttpServletRequest req) {
        String noticeId = req.getParameter("noticeId");
        AuthorizeNotice notice = permissionService.getAuthorizeByNoticeId(noticeId);
        notice.setDeal("true");
        permissionService.updateNotice(notice);
        return JSONObject.toJSON(notice);
    }


    private AuthorizeNotice BuildReplyNotice(AuthorizeNotice notice) {
        AuthorizeNotice replyNotice = new AuthorizeNotice();
        replyNotice.setNoticeId(UUID.randomUUID().toString().replace("-", ""));
        replyNotice.setSenderId(notice.getReceiverId());
        replyNotice.setSenderName(notice.getReceiverName());
        replyNotice.setReceiverId(notice.getSenderId());
        replyNotice.setReceiverName(notice.getSenderName());
        replyNotice.setDeploymentId(notice.getDeploymentId());
        replyNotice.setSendTime(new Date());
        replyNotice.setReply("false");
        replyNotice.setDeal("false");
        return replyNotice;
    }


}
