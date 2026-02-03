package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RefreshTokenIntercepter implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenIntercepter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取请求头中的token
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
           return true;
        }
        //3.存在，保存用户信息到ThreadLocal
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> useMap = stringRedisTemplate.opsForHash().entries(key);
        if(useMap.isEmpty()){
            return true;
        }
        //将查询到的Hash数据转为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(useMap, new UserDTO(), false);

        //存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);

        //刷新token有效期
        stringRedisTemplate.expire(key,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        //4.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //5.移除用户
        UserHolder.removeUser();
    }
}
