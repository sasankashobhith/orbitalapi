package com.bundee.msfw.servicefw.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration
//@EnableWebSecurity
//@ConditionalOnProperty(value = SSOConfig.SSO_ENABLED, havingValue = "false", matchIfMissing=true)
public class MethodBlocker {
	//This class Blocks TRACE. To block other methods we have MethodFilter
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .httpBasic().disable()
                .logout().disable()
                .authorizeRequests().anyRequest().permitAll();

        return http.build();
    }

}
