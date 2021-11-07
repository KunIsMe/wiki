package com.zhangkun.wiki.job;

 import com.zhangkun.wiki.service.DocService;
 import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

 import javax.annotation.Resource;

@Component
 public class DocJob {

    @Resource
    private DocService docService;

    /**
     * 每30秒更新电子书信息
     */
    @Scheduled(cron = "5/30 * * * * ?")
    public void cron() {
        docService.updateEbookInfo();
    }

 }
