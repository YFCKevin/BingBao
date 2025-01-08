package com.yfckevin.bingBao.config;

import com.yfckevin.bingBao.ConfigProperties;
import com.yfckevin.bingBao.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
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

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    private final ConfigProperties configProperties;
    private final JwtProperties jwtProperties;

    public SecurityConfig(ConfigProperties configProperties, JwtProperties jwtProperties) {
        this.configProperties = configProperties;
        this.jwtProperties = jwtProperties;
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

    @Bean
    public KeyPair keyPair() throws Exception {
        // 載入 Keystore
        Resource keystoreResource = new org.springframework.core.io.ClassPathResource(jwtProperties.getLocation());
        try (InputStream is = keystoreResource.getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, jwtProperties.getPassword().toCharArray());

            // 獲取私鑰
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(jwtProperties.getAlias(), jwtProperties.getPassword().toCharArray());
            if (privateKey == null) {
                throw new RuntimeException("未找到指定 alias 的私鑰: " + jwtProperties.getAlias());
            }

            // 獲取公鑰
            Certificate cert = keyStore.getCertificate(jwtProperties.getAlias());
            if (cert == null) {
                throw new RuntimeException("未找到指定 alias 的證書: " + jwtProperties.getAlias());
            }
            PublicKey publicKey = cert.getPublicKey();

            return new KeyPair(publicKey, privateKey);
        }
    }
}
