package com.yfckevin.bingBao.utils;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTool {
    private final JWTSigner jwtSigner;

    public JwtTool(KeyPair keyPair) {
        this.jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
    }

    /**
     * 创建 access-token
     * @param ttl
     * @return
     */
    public String createToken(Duration ttl) {
        // 1.生成jws
        return JWT.create()
                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .setSigner(jwtSigner)
                .sign();
    }

    /**
     * 解析token
     *
     * @param token token
     */
    public void parseToken(String token) {
        // 1.檢驗token是否為空值
        if (token == null) {
            System.out.println("未登錄");
            throw new RuntimeException("未登錄");
        }
        // 2.檢驗並解析JWT
        JWT jwt;
        try {
            jwt = JWT.of(token).setSigner(jwtSigner);
        } catch (Exception e) {
            System.out.println("無效token");
            throw new RuntimeException("無效token", e);
        }
        // 2.檢驗JWT是否有效
        if (!jwt.verify()) {
            // 驗證失敗
            System.out.println("無效token");
            throw new RuntimeException("無效token");
        }
        // 3.檢驗是否過期
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            System.out.println("token已經過期");
            throw new RuntimeException("token已經過期");
        }
    }
}