package org.example.utils;
import com.alibaba.druid.util.StringUtils;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Date;


@Data
@Component
@ConfigurationProperties(prefix = "jwt.token")
public class JwtHelper {

    private  long tokenExpiration; //有效时间,单位毫秒 1000毫秒 == 1秒
    private static String tokenSignKey;  //将变量改为静态

    // 增加静态setter方法以便配置属性能注入值
    public void setTokenSignKey(String tokenSignKey) {
        JwtHelper.tokenSignKey = tokenSignKey;
    }

    //生成token字符串
    public  String createToken(Long userId) {
        System.out.println("tokenExpiration = " + tokenExpiration);
        System.out.println("tokenSignKey = " + tokenSignKey);
        String token = Jwts.builder()

                .setSubject("YYGH-USER")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration*1000*60)) //单位分钟
                .claim("userId", userId)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }

    //从token字符串获取userid
    public Integer getUserId(String token) {
        if (StringUtils.isEmpty(token)) {
            System.out.println("JwtHelper: token为空");
            return null;
        }
        
        try {
            System.out.println("JwtHelper: 尝试解析token, tokenSignKey: " + (tokenSignKey != null ? "[已设置]" : "[未设置]"));
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            Integer userId = (Integer) claims.get("userId");
            System.out.println("JwtHelper: 成功解析token, 获取到userId: " + userId);
            return userId;
        } catch (Exception e) {
            System.out.println("JwtHelper: 解析token失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //判断token是否有效
    public  boolean isExpiration(String token){
        try {
            boolean isExpire = Jwts.parser()
                    .setSigningKey(tokenSignKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration().before(new Date());
            //没有过期，有效，返回false
            return isExpire;
        }catch(Exception e) {
            //过期出现异常，返回true
            return true;
        }
    }

    // 在Spring启动时输出tokenSignKey值
    @PostConstruct
    public void init() {
        System.out.println("JwtHelper初始化, tokenSignKey = " + tokenSignKey);
    }
}
