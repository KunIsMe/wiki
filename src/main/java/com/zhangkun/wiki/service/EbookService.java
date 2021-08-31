package com.zhangkun.wiki.service;

import com.zhangkun.wiki.domain.Ebook;
import com.zhangkun.wiki.domain.EbookExample;
import com.zhangkun.wiki.mapper.EbookMapper;
import com.zhangkun.wiki.req.EbookReq;
import com.zhangkun.wiki.resp.EbookResp;
import com.zhangkun.wiki.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EbookService {

    @Autowired
    private EbookMapper ebookMapper;

    public List<EbookResp> list(EbookReq req) {
        EbookExample ebookExample = new EbookExample();
        EbookExample.Criteria criteria = ebookExample.createCriteria();
        criteria.andNameLike("%" + req.getName() + "%");
        List<Ebook> ebookList = ebookMapper.selectByExample(ebookExample);

//        List<EbookResp> ebookRespList = new ArrayList<>();
//        for (Ebook ebook : ebookList) {
////            EbookResp ebookResp = new EbookResp();
////            BeanUtils.copyProperties(ebook, ebookResp);
//
//            // 对象复制
//            EbookResp ebookResp = CopyUtil.copy(ebook, EbookResp.class);
//            ebookRespList.add(ebookResp);
//        }

        // 列表复制
        List<EbookResp> ebookRespList = CopyUtil.copyList(ebookList, EbookResp.class);

        return ebookRespList;
    }
}
