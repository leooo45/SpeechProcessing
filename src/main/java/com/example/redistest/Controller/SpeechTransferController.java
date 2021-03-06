package com.example.redistest.Controller;

import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Service.IKDXFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/transfer")
public class SpeechTransferController {
    @Autowired
    private IKDXFService kdsfService;

    @PostMapping("/startTransfer")
    public String startTransfer(){
        System.out.println("接收到的文件路径sthtynynr");
        String message = "E:\\我的.mp4,4028809d70c248520170c25aeedf0000,audio";
        String filePath = message.split(",")[0];
        String resourceId = message.split(",")[1];
        String type = message.split(",")[2];
        TaskBean taskBean = new TaskBean();
        taskBean.setFilePath(filePath);
        taskBean.setType(type);
        taskBean.setResourceId(resourceId);
        kdsfService.addTask(taskBean);
        return "ok";
    }
}
