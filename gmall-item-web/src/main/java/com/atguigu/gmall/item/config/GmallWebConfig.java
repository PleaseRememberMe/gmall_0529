package com.atguigu.gmall.item.config;

import interceptot.LoginRequireInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//手动把这个主键导进来
@Import(LoginRequireInterceptor.class)
@Configuration
public class GmallWebConfig extends WebMvcConfigurerAdapter{
    @Autowired
    LoginRequireInterceptor loginRequireInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.addInterceptors(registry);
        //加拦截器
        registry.addInterceptor(loginRequireInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("*.jpg");
    }
}
