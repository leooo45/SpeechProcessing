package com.example.redistest.Utils.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FFMpegUtil {

    private String ffmpegEXE;

    public FFMpegUtil(String ffmpegEXE) {
        super();
        this.ffmpegEXE = ffmpegEXE;
    }
//    抽取音频转mp3
//    ffmpeg -i apple.mp4 -f mp3 -vn apple.mp3
    public void extractAudioFromVideo(String videoInputPath, String audioOutPutPath)throws Exception {
       File f=new File(videoInputPath);
       if(!f.exists()){
           System.out.println("输入文件不存在");
       }
        List<String> command = new ArrayList<>();
        command.add(ffmpegEXE);
        command.add("-i");
        command.add(videoInputPath);
        command.add("-f");
        command.add("mp3");
        command.add("-vn");
        command.add(audioOutPutPath);
        for (String c : command) {
            System.out.print(c + " ");
        }
        try{
            executiveOrder(command);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("抽取音频文件失败");
        }
        System.out.println("音频分离结束");
    }
    //执行命令
    public void executiveOrder(List<String> command) throws Exception {

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        InputStream errorStream = process.getErrorStream();
        InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
        BufferedReader br = new BufferedReader(inputStreamReader);

        String line = "";
        while ( (line = br.readLine()) != null ) {
        System.out.println(line);
        }

        if (br != null) {
            br.close();
        }
        if (inputStreamReader != null) {
            inputStreamReader.close();
        }
        if (errorStream != null) {
            errorStream.close();
        }

    }
    //音频切分
    // ffmpeg -i 124.mp3 -vn -acodec copy -ss 00:00:00 -t 00:01:32 output.mp3
    public void divideAudio(String inputfile, String outputfile)throws Exception {
        List<String> command = new ArrayList<>();
        command.add(ffmpegEXE);
        command.add("-i");
        command.add(inputfile);
        command.add("-vn");
        command.add("-acodec");
        command.add("copy");
        command.add("-ss");
        command.add("00:00:00");
        command.add("-t");
        command.add("00:30:00");
        command.add(outputfile);
        for (String c : command) {
            System.out.print(c + " ");
        }
        System.out.println("/n");
        try{
            executiveOrder(command);
        }catch (Exception e){
            e.printStackTrace();
          throw new Exception("音频文件拆分出错");
        }

    }
    public static void main(String[] args){
        FFMpegUtil ffMpegUtil=new FFMpegUtil("/Users/shaominchen/Documents/software/Util/ffmpeg-20200227-9b22254-macos64-static/bin/ffmpeg");
        try {
            ffMpegUtil.extractAudioFromVideo("/Users/shaominchen/Desktop/libei/libei_test.mp4","/Users/shaominchen/Desktop/libei/libei_test.mp3");
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("单例测试，结束音频测试");

    }

}