package com.hmdp;

import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdworker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for(int i=0;i<100;i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for(int i=0;i<300;i++) {
            es.submit(task);
        }
        latch.await();
        Long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }

    @Test
    void testHyperLoglog(){
        String[] values = new String[1000];
        int j=0;
        for(int i=0;i<1000000;i++) {
            j=i % 1000;
            values[j] = "user_" + i;
            if(j==999){
                //发送到redis
                stringRedisTemplate.opsForHyperLogLog().add("hl1",values);
            }
        }

        //统计数量
        Long count = stringRedisTemplate.opsForHyperLogLog().size("hl1");
        System.out.println("count = " + count);
    }
}
