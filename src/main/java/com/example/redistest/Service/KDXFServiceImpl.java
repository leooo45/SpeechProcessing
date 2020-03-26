package com.example.redistest.Service;

import com.alibaba.fastjson.JSON;
import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Bean.ApiResultDto;
import com.example.redistest.Config.RabbitMQConfig;
import com.example.redistest.Utils.util.KdxfWebUtil;
import com.example.redistest.Utils.util.SaveFileUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SignatureException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

@Service
public class KDXFServiceImpl implements IKDXFService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${queue.max_size}")
    int maxSize;

    @Autowired
    KdxfWebUtil kdxfWebUtil;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public void addTask(TaskBean taskBean) {
        System.out.println("加入一个任务");
//        amqpTemplate.convertAndSend(RabbitMQConfig.STATE_UPDATE_QUEUE,taskBean.getFilePath() + "," + "转换中");
        Queue<TaskBean> waitingQueue;
        try {
            //有可能得到的waitingqueue为空
            waitingQueue = (ArrayBlockingQueue) redisTemplate.opsForValue().get("waitingQueue");
        }
        catch (Exception e){
            System.out.println("我生成了新的队列！因为这个空队列反序列化失败啦！！！");
            waitingQueue = new ArrayBlockingQueue(1000);
        }
        //加入任务队列
        //如果运行队列（也就是redis中除了等待队列，其他所有的对象）满了就加入等待队列
        Set<String> keys = redisTemplate.keys("*");
//        for (int i = 0;i < 10;i++) {
//            System.out.println("----------addTask1------------\n\n\n");
//            String video = "D:\\downloadDir\\3.mp4";
//            File file = new File(video);
//            if (file.exists()) {
//                System.out.println("存在");
//            } else {
//                System.out.println("不存在");
//            }
//        }
        if (keys.size() < maxSize - 1) {
            if (waitingQueue.size() == 0) {
                startOneTask(taskBean);
            } else {
                TaskBean taskBean1 = (TaskBean) waitingQueue.poll();
                startOneTask(taskBean1);
                waitingQueue.add(taskBean);
                redisTemplate.opsForValue().set("waitingQueue", waitingQueue);
            }
        } else {
            waitingQueue.add(taskBean);
            redisTemplate.opsForValue().set("waitingQueue", waitingQueue);
            startOneTask(taskBean);
        }
    }


    //查询单个任务的状态
    @Override
    public void checkOneTaskStatus(TaskBean taskBean){
        try{
            System.out.println(taskBean.toString());
            String taskid=taskBean.getTaskId();
            if(taskid==null){
                //taskid 为空说明还没处理好 不查询直接返回
                System.out.println("找不到taskId" + taskid);
                System.out.println(taskBean.toString());
                return;
            }
            ApiResultDto taskProgress = kdxfWebUtil.getProgress(taskid);
            if (taskProgress.getOk() == 0) {
                if (taskProgress.getErr_no() != 0) {
                    System.out.println("任务失败：" + JSON.toJSONString(taskProgress));
                }
                String taskStatus = taskProgress.getData();
                if (JSON.parseObject(taskStatus).getInteger("status") == 9) {
                    System.out.println("任务完成！");
                    System.out.println("\r\n\r\n转写结果: " +kdxfWebUtil.getResult(taskid));
                    try{
                        //写入文件
                        String jsonSavePath=taskBean.getJsonPath();
                        //传入json文件保存地址，以及要保存的内容
                        SaveFileUtil.saveDataToFile(jsonSavePath,kdxfWebUtil.getResult(taskid));
                    }catch (Exception e){
                        e.printStackTrace();
                        return;
                    }
                    //任务完成
                    taskComplete(taskBean);
                    System.out.println("调试-任务处理成功");
                    return;
                }
                System.out.println("任务处理中：" + taskStatus);
            } else {
                System.out.println("获取任务进度失败！"+taskProgress.toString());
                System.out.println("获取任务进度失败！");
            }
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }



    //异常处理的专门模块
    @Override
    public void exceptionProcess() {


    }

    @Override
    public void startOneTask(TaskBean taskBean) {
//        for (int i = 0;i < 10;i++) {
//            System.out.println("----startOneTask------------------\n\n\n");
//            String video = "D:\\downloadDir\\3.mp4";
//            File file = new File(video);
//            if (file.exists()) {
//                System.out.println("存在");
//            } else {
//                System.out.println("不存在");
//            }
//        }
//        KdxfWebUtil kdxfWebUtil = new KdxfWebUtil(amqpTemplate,redisTemplate);
        kdxfWebUtil.startTask(taskBean);
    }



    @Override
    public void taskComplete(TaskBean taskBean) {
     /*若有任务完成
         则给文科平台API发消息（文件转换状态更新）
         队列（task-processing）删掉已完成任务
         队列（task-standby） 拿新的任务*/
        amqpTemplate.convertAndSend(RabbitMQConfig.STATE_UPDATE_QUEUE,taskBean.getFilePath() +  "," + "转换完成");
        amqpTemplate.convertAndSend(RabbitMQConfig.RESULT_QUEUE,taskBean.getFilePath() +  "," + taskBean.getJsonPath());
        redisTemplate.delete(taskBean.getResourceId());
        Queue waitingQueue;
        try {
            //有可能得到的waitingqueue为空
            waitingQueue = (ArrayBlockingQueue) redisTemplate.opsForValue().get("waitingQueue");
        }
        catch (Exception e){
            System.out.println("我生成了新的队列！因为这个空队列反序列化失败啦！！！");
            waitingQueue = new ArrayBlockingQueue(1000);
        }
//        Queue waitingQueue = (ArrayBlockingQueue) redisTemplate.opsForValue().get("waitingQueue");
        //加入任务队列
        //如果运行队列（也就是redis中除了等待队列，其他所有的对象）满了就加入等待队列
            if (waitingQueue.size() != 0) {
                TaskBean taskBean1 = (TaskBean) waitingQueue.poll();
                startOneTask(taskBean1);
                redisTemplate.opsForValue().set(waitingQueue, waitingQueue);
            }
        amqpTemplate.convertAndSend(taskBean.getFilePath() +  "," + taskBean.getJsonPath());
        redisTemplate.delete(taskBean.getResourceId());
    }
}
