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
     * 单个任务完成时的操作
     */
    void taskComplete(TaskBean taskBean);

    /**
     * @param taskBean
     * 传入单个任务类即可做处理，根据taskid查询任务状态
     */
    void checkOneTaskStatus(TaskBean taskBean);

}
