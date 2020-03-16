package com.example.redistest.Controller;

import com.example.redistest.Bean.TaskBean;
import com.example.redistest.Service.IKDXFService;
import com.example.redistest.Utils.util.FFMpegUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@Controller
//@RequestMapping("/transfer")
@RestController
public class SpeechTransferController {
    @Autowired
    private IKDXFService kdsfService;
//    @Value("${ffmpeg.filepath}")
//    public String FFMPEGEXE;
//    @Value("${kdxf_host.url}")
//    public  String LFASR_HOST ;
//    @Value("${kdxfConfig.appid}")
//    public String APPID;
//    @Value("${kdxfConfig.secretid}")
//    public String SECRET_KEY ;
//    @Value("${file_piece_size}")
//    public int SLICE_SICE ;

    @PostMapping("/startTransfer")
    public String startTransfer(String filepath,String id,String type){
        TaskBean taskBean = new TaskBean();
        taskBean.setFilePath(filepath);
        taskBean.setType(type);
        taskBean.setResourceId(id);
        //在数据进来的时候就生成json地址
        taskBean.setJsonPath(filepath.substring(0,filepath.lastIndexOf('.'))+".json");
         kdsfService.addTask(taskBean);
        //测试的时候打印一下传入的参数有没有空格
        return "--"+filepath+"--"+id+"--"+type+"--"+taskBean.getJsonPath()+"--";
    }
}
