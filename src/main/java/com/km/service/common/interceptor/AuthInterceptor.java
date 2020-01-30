package com.km.service.common.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.km.service.UserModule.domain.User;
import com.km.service.common.UnAuthToken;
import com.km.service.common.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Objects;

public class AuthInterceptor  implements HandlerInterceptor {
    @Autowired
    private RedisUtil redisutil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        String token = request.getHeader("token");
        UnAuthToken unAuthToken = method.getAnnotation(UnAuthToken.class);
        //如果controller中有些方法使用了该注解，则不会拦截
        if(null!=unAuthToken){
            return true;
        }
        if (StringUtils.isEmpty(token)) {
            response.getWriter().print("用户未登录，请登录后操作！");
            return false;
        }


        if (null == redisutil){
            BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            redisutil = (RedisUtil) factory.getBean("RedisUtil");
        }

        Object loginStatus = redisutil.get(token);
        if(Objects.isNull(loginStatus)){
            response.getWriter().print("token错误，请查看！");
            return false;
        }
        JSONObject object = JSONObject.parseObject(redisutil.get(token));
        User user = JSONObject.toJavaObject(object,User.class);
        request.setAttribute("user",user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
