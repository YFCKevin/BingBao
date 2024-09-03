package com.yfckevin.bingBao.config;

import com.yfckevin.bingBao.ConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ConfigProperties configProperties;

    public SecurityConfig(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 禁用 CSRF 保護
                .authorizeRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/bingBao_images/**").permitAll() // 設置靜態資源的訪問權限
                                .requestMatchers("/bingBao_files/**").permitAll()
                                .requestMatchers("/bingBao/**").permitAll()
                                .requestMatchers("/bingBao/api/**").authenticated() // 需要身份驗證的API
                ).httpBasic(withDefaults()).exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));  // 沒有提供身份認證資訊則回傳401

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username(configProperties.username)
                .password(passwordEncoder.encode(configProperties.password))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 使用 BCrypt 加密
    }

}
