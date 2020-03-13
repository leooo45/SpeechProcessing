package com.example.redistest.Service;

import com.alibaba.fastjson.JSON;
import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Utils.dto.ApiResultDto;
import com.example.redistest.Utils.util.KdxfWebUtil;
import com.example.redistest.Utils.util.SaveFileUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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

    KdxfWebUtil kdxfWebUtil = new KdxfWebUtil();

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public void addTask(TaskBean taskBean) {
//       Queue waitingQueue = (ArrayBlockingQueue)redisTemplate.opsForValue().get("waitingQueue");
//    //加入任务队列
//        //如果运行队列（也就是redis中除了等待队列，其他所有的对象）满了就加入等待队列
//        Set<String> keys = redisTemplate.keys("*");
//        if(keys.size() < maxSize - 1){
//            if(waitingQueue.size() == 0){
//                startOneTask(taskBean);
//            }
//            else {
//                TaskBean taskBean1 = (TaskBean)waitingQueue.poll();
//                startOneTask(taskBean1);
//                waitingQueue.add(taskBean);
//                redisTemplate.opsForValue().set(waitingQueue,waitingQueue);
//            }
//        }
//        else {
//            waitingQueue.add(taskBean);
//            redisTemplate.opsForValue().set(waitingQueue,waitingQueue);
//        }
        startOneTask(taskBean);
    }

    //获取转换结果，10秒一次查询转换
    @Override
//    @Scheduled(cron = "0/10 * * * * *")
    public void checkStatus() {
    //遍历正在转化的队列的taskId获取最新状态
//        Set<String> keys = redisTemplate.keys("*");
//        for(String key:keys){
//            //key为Redis中得到的所有的任务的资源id
//            if(!key.equals("waitingQueue")){
//                TaskBean task = (TaskBean)redisTemplate.opsForValue().get(key);
//                // 对得到的这个任务查询当前的状态
//                try{
//                    KdxfWebUtil kdxfWebUtil = new KdxfWebUtil();
//                    ApiResultDto taskProgress = kdxfWebUtil.getProgress(task.getTaskId());
//                    if (taskProgress.getOk() == 0) {
//                        if (taskProgress.getErr_no() != 0) {
//                            System.out.println("任务失败：" + JSON.toJSONString(taskProgress));
//                        }
//                        String taskStatus = taskProgress.getData();
//                        if (JSON.parseObject(taskStatus).getInteger("status") == 9) {
//                            System.out.println("任务完成！");
//                            System.out.println("\r\n\r\n转写结果: " +kdxfWebUtil.getResult(task.getTaskId()));
//                            try{
//                                //写入文件
//                                String jsonSavePath=task.getFilePath().substring(0,task.getFilePath().lastIndexOf("."))+".json";
//                                SaveFileUtil.saveDataToFile(jsonSavePath,kdxfWebUtil.getResult(task.getTaskId()));
//                            }catch (Exception e){
//                                e.printStackTrace();
////                    amqpTemplate.convertAndSend(mFilePath + "," + "转换出错");
//                                return;
//                            }
////                amqpTemplate.convertAndSend(mFilePath + "," + "转换成功");
//                             /*若有任务完成
//                             则给文科平台API发消息（文件转换状态更新）
//                             队列（task-processing）删掉已完成任务
//                             队列（task-standby） 拿新的任务*/
//                            taskComplete(task);
//                            return;
//                        }
//                        System.out.println("任务处理中：" + taskStatus);
//                    } else {
//                        System.out.println("获取任务进度失败！");
//
//                    }
//                    // 获取结果
//                } catch (SignatureException e) {
//                    e.printStackTrace();
////            amqpTemplate.convertAndSend(mFilePath + "," + "请求失败");
//                }
//
//            }
//
//        }


//单独测试 这一块需要加入taskid 我把它单独拿出来测试，手动赋值taskid
        try{
                    KdxfWebUtil kdxfWebUtil = new KdxfWebUtil();
                    String taskid="8e7850ecca18453fba966e9f258f792a";
                    ApiResultDto taskProgress = kdxfWebUtil.getProgress("e369cd2bfc3b4ef09ff2e210f6ab5169");
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
                                String jsonSavePath="/Users/shaominchen/Desktop/libei/libei_test.json";
                                SaveFileUtil.saveDataToFile(jsonSavePath,kdxfWebUtil.getResult(taskid));
                            }catch (Exception e){
                                e.printStackTrace();
//                    amqpTemplate.convertAndSend(mFilePath + "," + "转换出错");
                                return;
                            }
//                amqpTemplate.convertAndSend(mFilePath + "," + "转换成功");
                             /*若有任务完成
                             则给文科平台API发消息（文件转换状态更新）
                             队列（task-processing）删掉已完成任务
                             队列（task-standby） 拿新的任务*/
//                            taskComplete();
                            System.out.println("调试-任务处理成功");
                            return;
                        }
                        System.out.println("任务处理中：" + taskStatus);
                    } else {
                        System.out.println("获取任务进度失败！");

                    }
                    // 获取结果
                } catch (SignatureException e) {
                    e.printStackTrace();
//            amqpTemplate.convertAndSend(mFilePath + "," + "请求失败");
                }


    }
//查询单个任务的状态
    public void checkTaskStatus(TaskBean taskBean){
        try{
            KdxfWebUtil kdxfWebUtil = new KdxfWebUtil();
            String taskid=taskBean.getTaskId();
            ApiResultDto taskProgress = kdxfWebUtil.getProgress("e369cd2bfc3b4ef09ff2e210f6ab5169");
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
                        String jsonSavePath="/Users/shaominchen/Desktop/libei/libei_test.json";
                        SaveFileUtil.saveDataToFile(jsonSavePath,kdxfWebUtil.getResult(taskid));
                    }catch (Exception e){
                        e.printStackTrace();
//                    amqpTemplate.convertAndSend(mFilePath + "," + "转换出错");
                        return;
                    }
//                amqpTemplate.convertAndSend(mFilePath + "," + "转换成功");
                             /*若有任务完成
                             则给文科平台API发消息（文件转换状态更新）
                             队列（task-processing）删掉已完成任务
                             队列（task-standby） 拿新的任务*/
//                            taskComplete();
                    System.out.println("调试-任务处理成功");
                    return;
                }
                System.out.println("任务处理中：" + taskStatus);
            } else {
                System.out.println("获取任务进度失败！");

            }
            // 获取结果
        } catch (SignatureException e) {
            e.printStackTrace();
//            amqpTemplate.convertAndSend(mFilePath + "," + "请求失败");
        }

    }



    //异常处理的专门模块
    @Override
    public void exceptionProcess() {


    }

    @Override
    public void startOneTask(TaskBean taskBean) {
//        redisTemplate.opsForValue().set(taskBean.getResourceId(),taskBean);
        KdxfWebUtil kdxfWebUtil = new KdxfWebUtil();
        kdxfWebUtil.startTask(taskBean);
    }

    @Override
    public void taskComplete(TaskBean taskBean) {
     /*若有任务完成
         则给文科平台API发消息（文件转换状态更新）
         队列（task-processing）删掉已完成任务
         队列（task-standby） 拿新的任务*/
//        amqpTemplate.convertAndSend(taskBean.getFilePath() +  "," + taskBean.getJsonPath());
        redisTemplate.delete(taskBean.getResourceId());
    }
}
