package com.zhangkun.wiki.job;

 import com.zhangkun.wiki.service.DocService;
 import com.zhangkun.wiki.util.SnowFlake;
 import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
 public class DocJob {

    private final static Logger LOG = LoggerFactory.getLogger(DocJob.class);

    @Resource
    private DocService docService;

    @Resource
    private SnowFlake snowFlake;

    /**
     * 每30秒更新电子书信息
     */
    @Scheduled(cron = "5/30 * * * * ?")
    public void cron() {
        // 增加日志流水号
        MDC.put("LOG_ID", String.valueOf(snowFlake.nextId()));

        docService.updateEbookInfo();
    }

 }
