package com.jjt.platform.config.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<SimpleSecurityFilter> simpleSecurityFilterRegistration(SimpleSecurityFilter filter) {
        FilterRegistrationBean<SimpleSecurityFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(1);
        return registration;
    }
}
