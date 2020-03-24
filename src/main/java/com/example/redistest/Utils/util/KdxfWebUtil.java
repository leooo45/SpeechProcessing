package com.example.redistest.Utils.util;

import com.alibaba.fastjson.JSON;
import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Bean.ApiResultDto;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class KdxfWebUtil {

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${ffmpeg.filepath}")
    public String FFMPEGEXE;
    @Value("${kdxf_host.url}")
    public  String LFASR_HOST ;
    @Value("${kdxfConfig.appid}")
    public String APPID;
    @Value("${kdxfConfig.secretid}")
    public String SECRET_KEY ;
    @Value("${file_piece_size}")
    public int SLICE_SICE ;

    public static final String PREPARE = "/prepare";
    public static final String UPLOAD = "/upload";
    public static final String MERGE = "/merge";
    public static final String GET_RESULT = "/getResult";
    public static final String GET_PROGRESS = "/getProgress";
    public KdxfWebUtil() {
    }
    /**
     * 处理单个任务,完成文件的视频音频分离、切片上传、重组一系列操作
     * @param taskBean
     */
    public void startTask(TaskBean taskBean){
        //转换文件的地址
        String transferFilePath="";
        System.out.println("当前处理的任务为"+taskBean.toString());
        //判断task参数是否完整
        if(StringUtils.isEmpty(taskBean.getType())||StringUtils.isEmpty(taskBean.getFilePath())||StringUtils.isEmpty(taskBean.getResourceId())){
            System.out.println("调试-当前处理的信息不完整，发送mq，程序返回不再执行"+taskBean.toString());
            return;
        }

        if (!taskBean.getType().equals("视频") && !taskBean.getType().equals("音频")){
        //判断文件类型参数是否有问题

            return;
        }
        //视频抽取音频
        if(taskBean.getType().equals("视频")){
            System.out.println("调试-当前处理的文件类型错误，发送mq，程序返回不再执行"+taskBean.toString());
                taskBean.setTransferFilePath(extractAudioFromVideo(taskBean));
            }else {
            taskBean.setTransferFilePath(taskBean.getFilePath());
            }
        System.out.println("经过抽取音频，音频路径为 " + taskBean.getTransferFilePath());
        //音频文件处理
//        String outputFilePath = " C:\\downloadDir\\我的.mp3";
        String outputFilePath = taskBean.getTransferFilePath();
        File f=new File(outputFilePath);
        if(f.exists()){
            System.out.println("音频文件存在"+outputFilePath);
        }else {
            System.out.println("音频文件不存在"+outputFilePath);
            return;
        }

        File audio = new File(taskBean.getTransferFilePath());
        try (FileInputStream fis = new FileInputStream(audio)) {
            // 预处理
            String taskId = prepare(audio);
            //taskid赋值给taskbean
            taskBean.setTaskId(taskId);
            redisTemplate.opsForValue().set(taskBean.getResourceId(),taskBean);
            // 分片上传文件
            int len = 0;
            byte[] slice = new byte[SLICE_SICE];
            SliceIdGenerator generator = new SliceIdGenerator();
            while ((len =fis.read(slice)) > 0) {
                // 上传分片
                if (fis.available() == 0) {
                    slice = Arrays.copyOfRange(slice, 0, len);
                }
                uploadSlice(taskId, generator.getNextSliceId(), slice);
                }
                // 合并文件
            merge(taskId);
            } catch (SignatureException e) {
                e.printStackTrace();
            amqpTemplate.convertAndSend(taskBean.getFilePath()+ "," + "转换出错");
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            amqpTemplate.convertAndSend(taskBean.getFilePath()+ "," + "转换出错");
            } catch (IOException e2) {
                e2.printStackTrace();
            }catch (Exception e3){
               e3.printStackTrace();
            }
            //转换成功，传送mq给service更新状态或者是直接添加到redis
    }

    /**
     * 用于视频抽取音频
     * @param taskBean
     */
    public String extractAudioFromVideo(TaskBean taskBean){
//        for (int i = 0;i < 10;i++) {
//            System.out.println("-----------extractAudioFromVideo-----------\n\n\n");
//            String video = "D:\\downloadDir\\3.mp4";
//            File file = new File(video);
//            if (file.exists()) {
//                System.out.println("存在");
//            } else {
//                System.out.println("不存在");
//            }
//        }
        System.out.println("调试-当前处理的文件类型错误，发送mq，程序返回不再执行"+taskBean.toString());
        //生成视频目录下同名音频文件地址
        String outputFilePath=taskBean.getFilePath().substring(0,taskBean.getFilePath().lastIndexOf("."))+".mp3";
        System.out.println("预设的音频输出路径为 " + outputFilePath);
        //初始化ffmpeg
        FFMpegUtil ffmpeg = new FFMpegUtil(FFMPEGEXE);
        try{
            File file = new File(outputFilePath);
            if(file.exists()){
                file.delete();
            }
            System.out.println("我的视频路径 " + taskBean.getFilePath());
            ffmpeg.extractAudioFromVideo(taskBean.getFilePath(),outputFilePath);
        }catch (Exception e){
            System.out.println("调试-视频分离过程出错了");
            e.printStackTrace();
            return null;
        }
//        outputFilePath = " C:\\downloadDir\\我的.mp3";
       return outputFilePath;
    }

    public Map<String, String> getBaseAuthParam(String taskId) throws SignatureException {
        Map<String, String> baseParam = new HashMap<String, String>();
        String ts = String.valueOf(System.currentTimeMillis() / 1000L);
        baseParam.put("app_id", APPID);
        baseParam.put("ts", ts);
        baseParam.put("signa", EncryptUtil.HmacSHA1Encrypt(EncryptUtil.MD5(APPID + ts), SECRET_KEY));
        if (taskId != null) {
            baseParam.put("task_id", taskId);
        }
        return baseParam;
    }

    /**
     * 预处理
     *
     * @param audio     需要转写的音频
     * @return
     * @throws SignatureException
     */
    public String prepare(File audio) throws SignatureException {
        Map<String, String> prepareParam = getBaseAuthParam("b53ed5d07ef5432c8463add288194586");
        long fileLenth = audio.length();
        prepareParam.put("file_len", fileLenth + "");
        prepareParam.put("file_name", audio.getName());
        prepareParam.put("slice_num", (fileLenth/SLICE_SICE) + (fileLenth % SLICE_SICE == 0 ? 0 : 1) + "");
        /********************TODO 可配置参数********************/
        // 转写类型
//        prepareParam.put("lfasr_type", "0");
        // 开启分词
//        prepareParam.put("has_participle", "true");
        // 说话人分离
//        prepareParam.put("has_seperate", "true");
        // 设置多候选词个数
//        prepareParam.put("max_alternatives", "2");
        // 是否进行敏感词检出
//        prepareParam.put("has_sensitive", "true");
        // 敏感词类型
//        prepareParam.put("sensitive_type", "1");
        // 关键词
//        prepareParam.put("keywords", "科大讯飞,中国");
        /****************************************************/
        String response = HttpUtil.post(LFASR_HOST + PREPARE, prepareParam);
        if (response == null) {
            throw new RuntimeException("预处理接口请求失败！");
        }
        ApiResultDto resultDto = JSON.parseObject(response, ApiResultDto.class);
        String taskId = resultDto.getData();
        if (resultDto.getOk() != 0 || taskId == null) {
            throw new RuntimeException("预处理失败！" + response);
        }
        System.out.println("预处理成功, taskid：" + taskId);
        return taskId;
    }

    /**
     * 分片上传
     *
     * @param taskId        任务id
     * @param slice         分片的byte数组
     * @throws SignatureException
     */
    public void uploadSlice(String taskId, String sliceId, byte[] slice) throws SignatureException {
        Map<String, String> uploadParam = getBaseAuthParam(taskId);
        uploadParam.put("slice_id", sliceId);
        String response = HttpUtil.postMulti(LFASR_HOST + UPLOAD, uploadParam, slice);
        if (response == null) {
            throw new RuntimeException("分片上传接口请求失败！");
        }
        if (JSON.parseObject(response).getInteger("ok") == 0) {
            System.out.println("分片上传成功, sliceId: " + sliceId + ", sliceLen: " + slice.length);
            return;
        }
        System.out.println("params: " + JSON.toJSONString(uploadParam));
        throw new RuntimeException("分片上传失败！" + response + "|" + taskId);
    }

    /**
     * 文件合并
     *
     * @param taskId        任务id
     * @throws SignatureException
     */
    public void merge(String taskId) throws SignatureException {
        String response = HttpUtil.post(LFASR_HOST + MERGE, getBaseAuthParam(taskId));
        if (response == null) {
            throw new RuntimeException("文件合并接口请求失败！");
        }
        if (JSON.parseObject(response).getInteger("ok") == 0) {
            System.out.println("文件合并成功, taskId: " + taskId);
            return;
        }
        throw new RuntimeException("文件合并失败！" + response);
    }

    /**
     * 获取任务进度
     *
     * @param taskId        任务id
     * @throws SignatureException
     */
    public ApiResultDto getProgress(String taskId) throws SignatureException {
        String response = HttpUtil.post(LFASR_HOST + GET_PROGRESS, getBaseAuthParam(taskId));
        if (response == null) {
            throw new RuntimeException("获取任务进度接口请求失败！");
        }
        return JSON.parseObject(response, ApiResultDto.class);
    }

    /**
     * 获取转写结果
     *
     * @param taskId
     * @return
     * @throws SignatureException
     */
    public String getResult(String taskId) throws SignatureException {
        String responseStr = HttpUtil.post(LFASR_HOST + GET_RESULT, getBaseAuthParam(taskId));
        if (responseStr == null) {
            throw new RuntimeException("获取结果接口请求失败！");
        }
        ApiResultDto response = JSON.parseObject(responseStr, ApiResultDto.class);
        if (response.getOk() != 0) {
            throw new RuntimeException("获取结果失败！" + responseStr);
        }

        return response.getData();
    }
}

