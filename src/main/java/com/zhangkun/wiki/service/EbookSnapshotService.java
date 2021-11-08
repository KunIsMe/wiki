package com.zhangkun.wiki.service;

import com.zhangkun.wiki.mapper.EbookSnapshotMapperCust;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EbookSnapshotService {

    @Autowired
    private EbookSnapshotMapperCust ebookSnapshotMapperCust;

    public void genSnapshot() {
        ebookSnapshotMapperCust.genSnapshot();
    }
}
