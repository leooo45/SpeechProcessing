package com.example.redistest.Message;


import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Config.RabbitMQConfig;
import com.example.redistest.Service.IKDXFService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
//@RabbitListener(queues = RabbitMQConfig.TASK_START_QUEUE,containerFactory="rabbitListenerContainerFactory")
public class TaskStartReceiver {

    @Autowired
    private IKDXFService kdxfService;

    @RabbitHandler
    public void process(String message){
        String filePath = message.split(",")[0];
        String resourceId = message.split(",")[1];
        String type = message.split(",")[2];
        TaskBean taskBean = new TaskBean();
        taskBean.setFilePath(filePath);
        taskBean.setType(type);
        taskBean.setResourceId(resourceId);
        kdxfService.addTask(taskBean);
    }

}
