package com.example.redistest.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
//
//@Configuration
//public class RedisConfig {
//
//    //redis里面存放：
//    //taskBean对象(key为资源id，value为taskBean对象）
//    //正在运行的任务队列 runningQueue
//    //等待任务队列 waitingQueue
//
//    @Value("${queue.max_size}")
//    int size;
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//
//        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
//        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//        //使用StringRedisSerializer来序列化和反序列化redis的ke
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
//
//        //开启事务
//        redisTemplate.setEnableTransactionSupport(true);
//
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//
//        return redisTemplate;
//    }
//}
