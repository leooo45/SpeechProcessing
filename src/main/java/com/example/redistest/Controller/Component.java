package com.example.redistest.Controller;

import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Service.IKDXFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;

@Controller
public class Component {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IKDXFService kdxfService;

    @Scheduled(cron = "0/10 * * * * *")
    public void checkStatus() {

        //遍历正在转化的队列的taskId获取最新状态
        Set<String> keys = redisTemplate.keys("*");
        System.out.println("定时任务中的keys: ");
        keys.forEach(e ->{
            System.out.println("\n" + e.toString());
        });
        for (String key : keys) {
            //key为Redis中得到的所有的任务的资源id
            if (!key.equals("waitingQueue")) {
                TaskBean task = (TaskBean) redisTemplate.opsForValue().get(key);
                // 对得到的这个任务查询当前的状态
                kdxfService.checkOneTaskStatus(task);
            }
        }
    }
}
