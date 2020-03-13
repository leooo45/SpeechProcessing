package com.example.redistest.Utils.util;

import java.io.*;

public class SaveFileUtil {

    /**
     *
     * @param fileName 写入的文件名称
     * @param data 写入的数据
     */
    public static void saveDataToFile(String fileName, String data) throws Exception {
        BufferedWriter writer = null;
        File file = new File(fileName);
        //如果文件不存在，则新建一个
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {

                e.printStackTrace();
                throw new Exception("文件创建失败，创建的文件路径是："+fileName);
            }
        }
        //写入
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("文件写入数据出错");
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception("文件写入无法关闭");
            }
        }
        System.out.println("文件写入成功！");
    }
}
