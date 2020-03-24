package com.example.redistest.Bean;

import lombok.Data;

@Data
public class TaskBean {

    //当前转化的文件的taskid,预处理后科大讯飞返回，用于查询任务状态
    private String taskId = "";

    //文件本地绝对地址
    private String filePath = "";

    //处理的资源的id
    private String resourceId = "";

    //文件类型
    private String type = "";

    //json文件存储的地址
    private String jsonPath = "";

    //实际进行音频转化的文件地址
    private String transferFilePath;
}
