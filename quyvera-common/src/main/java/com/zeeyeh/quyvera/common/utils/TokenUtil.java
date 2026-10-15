package com.zeeyeh.quyvera.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * token工具类
 * @author Qinloren
 */
@Data
@AllArgsConstructor
public class TokenUtil {
    private String secret;
    private int expire;

    public static TokenUtil builder(String secret, int expire) {
        return new TokenUtil(secret, expire);
    }

    /**
     * 生成token
     * @return token字符串
     */
    public String generateToken() {
        Map<String, Object> map = new HashMap<>();
        int count = 5 + RandomUtil.randomInt(6);
        for (int i = 0; i < count; i++) {
            map.put(
                    RandomUtil.randomString(6),
                    RandomUtil.randomString(16)
            );
        }
        map.put("un", RandomUtil.randomString(6));
        map.put("isAdmin", RandomUtil.randomBoolean());
        map.put("role", "ROLE_" + RandomUtil.randomString(4));
        return generateToken(RandomUtil.randomString(16), map);
    }

    /**
     * 生成token
     * @param map token内容
     * @return token字符串
     */
    public String generateToken(Map<String, Object> map) {
        return generateToken(RandomUtil.randomString(16), map);
    }

    /**
     * 生成token
     * @param userId 用户id
     * @param map token内容
     * @return token字符串
     */
    public String generateToken(String userId, Map<String, Object> map) {
        String subject = StrUtil.isNotEmpty(userId) ? RandomUtil.randomString(8) : userId;
        Map<String, Object> claims = new HashMap<>(map);
        SecretKey secretKey = Keys.hmacShaKeyFor(getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(DateUtil.offsetSecond(new Date(), expire))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 验证token
     * @param token token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        token = token.replace("Bearer ", "");
        SecretKey secretKey = Keys.hmacShaKeyFor(getSecret().getBytes(StandardCharsets.UTF_8));
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
