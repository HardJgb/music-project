package org.example.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * JWT工具类，用于生成和解析token
 */
public class JwtUtils {

    // 签名密钥
    private static final String SECRET_KEY = "musicPlayerSecretKey";
    
    // token过期时间（7天）
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000;
    
    /**
     * 生成token
     * @param userId 用户ID
     * @return token字符串
     */
    public static String generateToken(Integer userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);
        
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    
    /**
     * 从token中获取用户ID
     * @param token token字符串
     * @return 用户ID
     */
    public static Integer getUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            
            String userId = claims.getSubject();
            return Integer.parseInt(userId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 验证token是否有效
     * @param token token字符串
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 