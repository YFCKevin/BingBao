package com.yfckevin.bingBao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "token")
public class Token {
    @Id
    private String id;
    private String shortStr;
    private String jwtToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortStr() {
        return shortStr;
    }

    public void setShortStr(String shortStr) {
        this.shortStr = shortStr;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
