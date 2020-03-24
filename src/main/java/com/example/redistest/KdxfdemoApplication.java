package com.example.redistest;

import com.example.redistest.Utils.util.FFMpegUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@EnableScheduling//开启定时访问
@EnableAsync//开启异步任务
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class KdxfdemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(KdxfdemoApplication.class, args);
    }

}
