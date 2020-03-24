package com.example.redistest.Config;

import com.example.redistest.Bean.TaskBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
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
//        if(!redisTemplate.hasKey("waitingQueue")) {
            Queue<TaskBean> queue = new ArrayBlockingQueue(1000);
            redisTemplate.opsForValue().set("waitingQueue", queue);
//            TaskBean taskBean = new TaskBean();
//            taskBean.setFilePath("111");
//            taskBean.setType("22");
//            taskBean.setResourceId("22");
//            //在数据进来的时候就生成json地址
//            taskBean.setJsonPath("22");
//        }
        return redisTemplate;
    }
}
