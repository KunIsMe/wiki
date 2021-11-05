package com.zhangkun.wiki.service;

import com.zhangkun.wiki.domain.Content;
import com.zhangkun.wiki.domain.ContentExample;
import com.zhangkun.wiki.domain.Doc;
import com.zhangkun.wiki.domain.DocExample;
import com.zhangkun.wiki.mapper.ContentMapper;
import com.zhangkun.wiki.mapper.DocMapper;
import com.zhangkun.wiki.req.DocQueryReq;
import com.zhangkun.wiki.req.DocSaveReq;
import com.zhangkun.wiki.resp.DocQueryResp;
import com.zhangkun.wiki.util.CopyUtil;
import com.zhangkun.wiki.util.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class DocService {

    @Autowired
    private DocMapper docMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private SnowFlake snowFlake;

    /**
     * 文档列表请求（不分页）
     * @param req
     * @return
     */
    public List<DocQueryResp> all(DocQueryReq req) {
        DocExample docExample = new DocExample();
        docExample.setOrderByClause("sort asc");
        DocExample.Criteria criteria = docExample.createCriteria();
        if(!ObjectUtils.isEmpty(req.getName())) {
            criteria.andNameLike("%" + req.getName() + "%");
        }
        List<Doc> docList = docMapper.selectByExample(docExample);

        // 列表复制
        List<DocQueryResp> docRespList = CopyUtil.copyList(docList, DocQueryResp.class);

        return docRespList;
    }

    /**
     * 文档编辑或增加
     * @param req
     */
    public void save(DocSaveReq req) {
        Doc doc = CopyUtil.copy(req, Doc.class);
        Content content = CopyUtil.copy(req, Content.class);
        if(ObjectUtils.isEmpty(req.getId())){
            // 新增电子书
            doc.setId(snowFlake.nextId());
            docMapper.insert(doc);

            content.setId(doc.getId());
            contentMapper.insert(content);
        } else {
            // 编辑电子书
            docMapper.updateByPrimaryKey(doc);

            int count = contentMapper.updateByPrimaryKeyWithBLOBs(content);
            if (count == 0) {
                contentMapper.insert(content);
            }
        }
    }

    /**
     * 文档删除
     * @param id
     */
    public void delete(Long id) {
        docMapper.deleteByPrimaryKey(id);
    }

    public void delete(List<String> ids) {
        DocExample docExample = new DocExample();
        DocExample.Criteria criteria = docExample.createCriteria();
        criteria.andIdIn(ids);

        ContentExample contentExample = new ContentExample();
        ContentExample.Criteria criteriaContent = contentExample.createCriteria();
        criteriaContent.andIdIn(ids);

        docMapper.deleteByExample(docExample);
        contentMapper.deleteByExample(contentExample);
    }

    public String findContent(Long id) {
        Content content = contentMapper.selectByPrimaryKey(id);
        return content.getContent();
    }
}
