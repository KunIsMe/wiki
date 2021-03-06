package com.zhangkun.wiki.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhangkun.wiki.domain.DocExample;
import com.zhangkun.wiki.domain.Ebook;
import com.zhangkun.wiki.domain.EbookExample;
import com.zhangkun.wiki.mapper.DocMapper;
import com.zhangkun.wiki.mapper.EbookMapper;
import com.zhangkun.wiki.req.EbookQueryReq;
import com.zhangkun.wiki.req.EbookSaveReq;
import com.zhangkun.wiki.resp.EbookQueryResp;
import com.zhangkun.wiki.resp.PageResp;
import com.zhangkun.wiki.util.CopyUtil;
import com.zhangkun.wiki.util.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class EbookService {

    @Autowired
    private EbookMapper ebookMapper;

    @Autowired
    private DocMapper docMapper;

    @Autowired
    private SnowFlake snowFlake;

    /**
     * 电子书列表请求
     * @param req
     * @return
     */
    public PageResp<EbookQueryResp> list(EbookQueryReq req) {
        EbookExample ebookExample = new EbookExample();
        EbookExample.Criteria criteria = ebookExample.createCriteria();
        if(!ObjectUtils.isEmpty(req.getName())) {
            criteria.andNameLike("%" + req.getName() + "%");
        }
        if(!ObjectUtils.isEmpty(req.getCategoryId2())) {
            criteria.andCategory2IdEqualTo(req.getCategoryId2());
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
        List<EbookQueryResp> ebookRespList = CopyUtil.copyList(ebookList, EbookQueryResp.class);

        PageResp<EbookQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(ebookRespList);

        return pageResp;
    }

    /**
     * 电子书编辑或增加
     * @param req
     */
    public void save(EbookSaveReq req) {
        Ebook ebook = CopyUtil.copy(req, Ebook.class);
        if(ObjectUtils.isEmpty(req.getId())){
            // 新增电子书
            ebook.setId(snowFlake.nextId());
            ebook.setDocCount(0);
            ebook.setViewCount(0);
            ebook.setVoteCount(0);
            ebookMapper.insert(ebook);
        } else {
            // 编辑电子书
            ebookMapper.updateByPrimaryKey(ebook);
        }
    }

    /**
     * 电子书删除
     * @param id
     */
    public void delete(Long id) {
        ebookMapper.deleteByPrimaryKey(id);

        // 同时要删除掉相对应的文档表数据
        DocExample docExample = new DocExample();
        DocExample.Criteria criteria = docExample.createCriteria();
        criteria.andEbookIdEqualTo(id);
        docMapper.deleteByExample(docExample);
    }
}
