package com.yfckevin.bingBao;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String location;
    private String password;
    private String alias;
    private Duration tokenTTL = Duration.ofMinutes(10);

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Duration getTokenTTL() {
        return tokenTTL;
    }

    public void setTokenTTL(Duration tokenTTL) {
        this.tokenTTL = tokenTTL;
    }
}
