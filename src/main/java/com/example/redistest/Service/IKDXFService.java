package com.example.redistest.Service;

import com.example.redistest.Bean.TaskBean;

import java.util.List;

/**
 *
 */
public interface IKDXFService {

    /**
     * 新建任务，加入任务队列
     *
     */
    void addTask(TaskBean taskBean);

    /**
     * 10秒一次更新当前状态，并做相应处理
     */
    void checkStatus();

    /**
     * 监听文件转化状态，处理文件转化失败情况
     */
    void exceptionProcess();


    /**
     * @param taskBean
     * 开始一个处理转换任务的线程
     */
    void startOneTask(TaskBean taskBean);

    /**
     * @param taskBean
     * 任务完成时的操作
     */
    void taskComplete(TaskBean taskBean);

}
