package com.example.redistest.Service;

import com.alibaba.fastjson.JSON;
import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Bean.ApiResultDto;
import com.example.redistest.Utils.util.KdxfWebUtil;
import com.example.redistest.Utils.util.SaveFileUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.util.Set;

@Service
public class KDXFServiceImpl implements IKDXFService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${queue.max_size}")
    int maxSize;
    @Autowired
    KdxfWebUtil kdxfWebUtil = new KdxfWebUtil();

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Override
    public void addTask(TaskBean taskBean) {
//        Queue waitingQueue = (ArrayBlockingQueue)redisTemplate.opsForValue().get("waitingQueue");
        //加入任务队列
        //如果运行队列（也就是redis中除了等待队列，其他所有的对象）满了就加入等待队列
//        Set<String> keys = redisTemplate.keys("*");
//        if(keys.size() < maxSize - 1){
//            if(waitingQueue.size() == 0){
                startOneTask(taskBean);
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
    }

    //获取转换结果，10秒一次查询转换
    @Override
    @Scheduled(cron = "0/10 * * * * *")
    public void checkStatus() {
//    遍历正在转化的队列的taskId获取最新状态
        Set<String> keys = redisTemplate.keys("*");

        for(String key:keys){
            //key为Redis中得到的所有的任务的资源id
//            if(!key.equals("waitingQueue")) {
//                这里只要传一个taskbean,遍历队列 然后取出来
//            checkOneTaskStatus(taskBean);
                /***********************************************/
                //测试单个task 只要传入taskID 以及json地址
//            TaskBean taskBean=new TaskBean();
//            taskBean.setJsonPath("/Users/shaominchen/Desktop/libei/libei_test.json");
//            taskBean.setTaskId("7f7b59663eb1486a927b4a658e9b966c");
//            checkOneTaskStatus(taskBean);
                /************************************************/
//            }

        }
    }

    //查询单个任务的状态
    @Override
    public void checkOneTaskStatus(TaskBean taskBean){
        try{
            String taskid=taskBean.getTaskId();
            if(taskid==null){
                //taskid 为空说明还没处理好 不查询直接返回
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
//        redisTemplate.opsForValue().set(taskBean.getResourceId(),taskBean);
        kdxfWebUtil.startTask(taskBean);
    }

    @Override
    public void taskComplete(TaskBean taskBean) {
     /*若有任务完成
         则给文科平台API发消息（文件转换状态更新）
         队列（task-processing）删掉已完成任务
         队列（task-standby） 拿新的任务*/
//        amqpTemplate.convertAndSend(taskBean.getFilePath() +  "," + taskBean.getJsonPath());
//        redisTemplate.delete(taskBean.getResourceId());
    }
}
