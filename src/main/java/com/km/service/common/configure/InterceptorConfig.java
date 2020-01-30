package com.km.service.common.configure;


import com.km.service.common.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 实现 WebMvcConfigurer 不会导致静态资源被拦截
        registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
    }
}
