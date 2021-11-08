package com.zhangkun.wiki.service;

import com.zhangkun.wiki.mapper.EbookSnapshotMapperCust;
import com.zhangkun.wiki.resp.StatisticResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EbookSnapshotService {

    @Autowired
    private EbookSnapshotMapperCust ebookSnapshotMapperCust;

    public void genSnapshot() {
        ebookSnapshotMapperCust.genSnapshot();
    }

    /**
     * 获取首页数值数据：总阅读数、总点赞数、今日阅读数、今日点赞数
     * @return
     */
    public List<StatisticResp> getStatistic() {
        List<StatisticResp> statisticResp = ebookSnapshotMapperCust.getStatistic();
        return statisticResp;
    }

    /**
     * 30天数值统计（阅读数、点赞数）【每日新增】
     * @return
     */
    public List<StatisticResp> get30Statistic() {
        List<StatisticResp> statisticResp = ebookSnapshotMapperCust.get30Statistic();
        return statisticResp;
    }
}
