package com.example.redistest.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@Component
public class SystemInitializer {
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${queue.max_size}")
    int maxSize;

    @Bean
    public RedisTemplate getRedisTemplate(){
        if(!redisTemplate.hasKey("waitingQueue")) {
            Queue queue = new ArrayBlockingQueue(1000);
            redisTemplate.opsForValue().set("waitingQueue", queue);
        }
        return redisTemplate;
    }
}
