package com.example.redistest.Message;


import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Config.RabbitMQConfig;
import com.example.redistest.Service.IKDXFService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
import java.io.File;

=======
>>>>>>> 35c5135d7db9e8063acedd265146a9c5ee7dbe73
@Component
@RabbitListener(queues = RabbitMQConfig.TASK_START_QUEUE,containerFactory="rabbitListenerContainerFactory")
public class TaskStartReceiver {

    @Autowired
    private IKDXFService kdxfService;

    @RabbitHandler
    public void process(String message){
<<<<<<< HEAD
        System.out.println(message);
=======
>>>>>>> 35c5135d7db9e8063acedd265146a9c5ee7dbe73
        String filePath = message.split(",")[0];
        String resourceId = message.split(",")[1];
        String type = message.split(",")[2];
        String jsonPath = filePath.substring(0,filePath.lastIndexOf(".")) + ".json";
<<<<<<< HEAD
        System.out.println("文件路径是" + filePath);
=======
>>>>>>> 35c5135d7db9e8063acedd265146a9c5ee7dbe73
        TaskBean taskBean = new TaskBean();
        taskBean.setFilePath(filePath);
        taskBean.setType(type);
        taskBean.setJsonPath(jsonPath);
        taskBean.setResourceId(resourceId);
        kdxfService.addTask(taskBean);
    }
}
