package com.lyh.reversi_online.config;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<Filter> loggingFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                Cookie[] cookies = ((HttpServletRequest) request).getCookies();

                // System.out.println("有經過過濾器的請求:" + ((HttpServletRequest) request).getRequestURI());
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("player_uuid".equals(cookie.getName())) {
                            chain.doFilter(request, response); // 有 player_uuid 就放行，沒有就先不管它。
                        }
                    }
                }
            }
        });

        // 要攔截的請求。
        registrationBean.addUrlPatterns(
                "/images/*",
                "/pages/*",
                "/scripts/*",
                "/styles/*",
                "/queue",
                "/cancelQueue",
                "/backToLooby",
                "/watch/*");

        // 多個 Filter 時的優先順序，值越小越先執行。
        registrationBean.setOrder(1);

        return registrationBean;
    }
}