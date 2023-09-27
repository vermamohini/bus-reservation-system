package com.tcs.bookingms.security;

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
    private static final String SECURED_ROLE_USER = "hasRole('ROLE_user')";
    private static final String SECURED_PATTERN = "/**";
    private static final String SECURED_PATTERN_CONFIRM_BOOKING = "/confirmBooking";
    private static final String SECURED_PATTERN_SAVE_BOOKING = "/saveBooking";
    private static final String SECURED_PATTERN_CANCEL_BOOKING = "/cancelBooking";


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
                .antMatchers(SECURED_PATTERN_CONFIRM_BOOKING)
                .access(SECURED_ROLE_ADMIN).and().authorizeRequests()
                .antMatchers(SECURED_PATTERN_CONFIRM_BOOKING).not()
                .access(SECURED_ROLE_USER)
                .antMatchers(SECURED_PATTERN_SAVE_BOOKING)
                .access(SECURED_ROLE_ADMIN + " or " + SECURED_ROLE_USER)
                .antMatchers(SECURED_PATTERN_CANCEL_BOOKING)
                .access(SECURED_ROLE_ADMIN + " or " + SECURED_ROLE_USER);
    }
}