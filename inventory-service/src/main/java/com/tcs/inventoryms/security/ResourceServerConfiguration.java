package com.tcs.inventoryms.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "microservice";
    private static final String SECURED_ROLE_ADMIN = "hasRole('ROLE_admin')";
    private static final String SECURED_PATTERN = "/**";

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(RESOURCE_ID);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, SECURED_PATTERN).permitAll()
                .and()
                .requestMatchers()                
                .antMatchers(SECURED_PATTERN).and().authorizeRequests()
                .antMatchers(SECURED_PATTERN)
                .access(SECURED_ROLE_ADMIN);
    }
}