package com.zhangkun.wiki.service;

import com.zhangkun.wiki.domain.Doc;
import com.zhangkun.wiki.domain.DocExample;
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
        if(ObjectUtils.isEmpty(req.getId())){
            // 新增电子书
            doc.setId(snowFlake.nextId());
            docMapper.insert(doc);
        } else {
            // 编辑电子书
            docMapper.updateByPrimaryKey(doc);
        }
    }

    /**
     * 文档删除
     * @param id
     */
    public void delete(Long id) {
        docMapper.deleteByPrimaryKey(id);
    }
}
