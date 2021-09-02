package com.zhangkun.wiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhangkun.wiki.domain.Ebook;
import com.zhangkun.wiki.domain.EbookExample;
import com.zhangkun.wiki.mapper.EbookMapper;
import com.zhangkun.wiki.req.EbookReq;
import com.zhangkun.wiki.resp.EbookResp;
import com.zhangkun.wiki.resp.PageResp;
import com.zhangkun.wiki.util.CopyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class EbookService {

    @Autowired
    private EbookMapper ebookMapper;

    public PageResp<EbookResp> list(EbookReq req) {
        EbookExample ebookExample = new EbookExample();
        EbookExample.Criteria criteria = ebookExample.createCriteria();
        if(!ObjectUtils.isEmpty(req.getName())) {
            criteria.andNameLike("%" + req.getName() + "%");
        }
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Ebook> ebookList = ebookMapper.selectByExample(ebookExample);

        PageInfo<Ebook> pageInfo = new PageInfo<>(ebookList);
        System.out.println("总行数：" + pageInfo.getTotal());
        System.out.println("总页数：" + pageInfo.getPages());

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

        PageResp<EbookResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(ebookRespList);

        return pageResp;
    }
}
