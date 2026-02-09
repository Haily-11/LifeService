package com.hmdp.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RedisIdWorker {

    /**
     * 开始时间戳
     */
    private static final long BEGIN_TIMESTAMP = 1770549172L;
    /**
     * 序列号位数
     */
    private static final long COUNT_BITS = 32;
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 利用redis自增生成全局唯一ID
     * @param keyPrefix
     * @return 全局唯一ID
     */
    public Long nextId(String keyPrefix) {
        //Redis自增ID策略：①每天一个key，方便统计订单量；②ID构造是时间戳+序列号计数器
        //1.生成时间戳
         long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
         long timestamp = now - BEGIN_TIMESTAMP;
        //2.生成序列号
        //获取当前日期，精确到天
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
         long sequence = stringRedisTemplate.opsForValue().increment("icr:"+keyPrefix+":"+date);
        //3.拼接返回
        return timestamp << COUNT_BITS | sequence;
    }


}
