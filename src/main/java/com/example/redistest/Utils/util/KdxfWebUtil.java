package com.example.redistest.Utils.util;

import com.alibaba.fastjson.JSON;
import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Utils.dto.ApiResultDto;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class KdxfWebUtil {

    public KdxfWebUtil(AmqpTemplate amqpTemplate,RedisTemplate redisTemplate){
        this.amqpTemplate = amqpTemplate;
        this.redisTemplate = redisTemplate;
    }
    private AmqpTemplate amqpTemplate;

    private RedisTemplate redisTemplate;

    public static String LFASR_HOST="http://raasr.xfyun.cn/api" ;
    private String APPID = "5e4cb2a5";
    public String SECRET_KEY = "fb3e810f1cbfb391594241d6ddfd1c6e";
    public static final int SLICE_SICE=10485760 ;

//    @Value("${ffmpeg.filepath}")
    public String FFMPEGEXE="D:\\ffmpeg-20200309-608b8a8-win64-static\\bin\\ffmpeg";
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
        if(StringUtils.isEmpty(taskBean.getType())||StringUtils.isEmpty(taskBean.getFilePath())||StringUtils.isEmpty(taskBean.getResourceId())){
            System.out.println("调试-当前处理的信息不完整，发送mq，程序返回不再执行"+taskBean.toString());
            return;
        }
        if (!taskBean.getType().equals("视频") && !taskBean.getType().equals("音频")){
            System.out.println("调试-当前处理的文件类型错误，发送mq，程序返回不再执行"+taskBean.toString());
            return;
        }
        //视频抽取音频
        if(taskBean.getType().equals("视频")){
            String filepath=taskBean.getFilePath();
            //生成视频目录下同名音频文件地址
            String outputFilePath=filepath.substring(0,filepath.lastIndexOf("."))+".mp3";
            System.out.println("调试-生成分离的音频地址为"+outputFilePath);
            //初始化ffmpeg
            FFMpegUtil ffmpeg = new FFMpegUtil(FFMPEGEXE);
            try{
                ffmpeg.extractAudioFromVideo(filepath,outputFilePath);
                System.out.println("调试-视频分离的音频路径为"+outputFilePath);
            }catch (Exception e){
                System.out.println("调试-视频分离过程出错了");
                // 发送视频音频分离错误MQ 打印错误不再继续
                e.printStackTrace();
//                amqpTemplate.convertAndSend(taskBean.getFilePath() + "," + "转换出错");
                return;
            }
            System.out.println("调试-音频处理结束进入音频切割");

            transferFilePath=outputFilePath;
            /**
             * 这里是否在转换结束再次判断文件是否存在，如果不存在采取措施？？？
             */
            }else {
                transferFilePath=taskBean.getFilePath();
            }

        File audio = new File(transferFilePath);
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
            System.out.println("taskid is ---"+taskId);
            } catch (SignatureException e) {
                e.printStackTrace();
//            amqpTemplate.convertAndSend(taskBean.getResourceId()+ "," + "转换出错");
            } catch (FileNotFoundException e1) {
                //文件不存在 MQ
                e1.printStackTrace();
//            amqpTemplate.convertAndSend(filePath + "," + "转换出错");
            } catch (IOException e2) {
                //文件读取异常 MQ
                e2.printStackTrace();
//            amqpTemplate.convertAndSend(filePath + "," + "转换出错");
            }catch (Exception e3){
               e3.printStackTrace();
//            amqpTemplate.convertAndSend(filePath + "," + "转换出错");
            }
            //转换成功，传送mq给service更新状态
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
            /**
             * 发送MQ 预处理接口请求失败！
             */
            throw new RuntimeException("预处理接口请求失败！");
        }
        ApiResultDto resultDto = JSON.parseObject(response, ApiResultDto.class);
        String taskId = resultDto.getData();
        if (resultDto.getOk() != 0 || taskId == null) {
            /**
             * 发送MQ 预处理失败！
             */
            throw new RuntimeException("预处理失败！" + response);
        }

        System.out.println("预处理成功, taskid：" + taskId);
        /**
         * 发送MQ 预处理成功
         */
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
            /**
             * 发送MQ -- 分片上传接口请求失败
             */
            throw new RuntimeException("分片上传接口请求失败！");
        }
        if (JSON.parseObject(response).getInteger("ok") == 0) {
            /**
             * 发送MQ -- 分片上传成功
             * sliceId: " + sliceId + ", sliceLen: " + slice.length
             */
            System.out.println("分片上传成功, sliceId: " + sliceId + ", sliceLen: " + slice.length);
            return;
        }

        System.out.println("params: " + JSON.toJSONString(uploadParam));
        /**
         * 发送MQ -- 分片上传失败！" + response + "|" + taskId
         */
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
            Date date=new Date();
            System.out.println(date.toString());
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

