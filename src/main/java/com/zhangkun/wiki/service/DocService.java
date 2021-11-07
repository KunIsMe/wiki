package com.zhangkun.wiki.service;

import com.zhangkun.wiki.domain.Content;
import com.zhangkun.wiki.domain.ContentExample;
import com.zhangkun.wiki.domain.Doc;
import com.zhangkun.wiki.domain.DocExample;
import com.zhangkun.wiki.exception.BusinessException;
import com.zhangkun.wiki.exception.BusinessExceptionCode;
import com.zhangkun.wiki.mapper.ContentMapper;
import com.zhangkun.wiki.mapper.DocMapper;
import com.zhangkun.wiki.mapper.DocMapperCust;
import com.zhangkun.wiki.req.DocSaveReq;
import com.zhangkun.wiki.resp.DocQueryResp;
import com.zhangkun.wiki.util.CopyUtil;
import com.zhangkun.wiki.util.RedisUtil;
import com.zhangkun.wiki.util.RequestContext;
import com.zhangkun.wiki.util.SnowFlake;
import com.zhangkun.wiki.websocket.WebSocketServer;
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
    private DocMapperCust docMapperCust;

    @Autowired
    private SnowFlake snowFlake;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 文档列表请求（不分页）
     * @param
     * @return
     */
    public List<DocQueryResp> all(Long ebookId) {
        DocExample docExample = new DocExample();
        docExample.createCriteria().andEbookIdEqualTo(ebookId);
        docExample.setOrderByClause("sort asc");
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
            // 新增文档
            doc.setId(snowFlake.nextId());
            doc.setViewCount(0);
            doc.setVoteCount(0);
            docMapper.insert(doc);

            content.setId(doc.getId());
            contentMapper.insert(content);
        } else {
            // 编辑文档
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

    /**
     * 查找对应文档内容
     * @param id
     * @return
     */
    public String findContent(Long id) {
        Content content = contentMapper.selectByPrimaryKey(id);
        // 文档阅读数+1
        docMapperCust.increaseViewCount(id);
        if(ObjectUtils.isEmpty(content)){
            return "";
        } else {
            return content.getContent();
        }
    }

    /**
     * 点赞
     * @param id
     */
    public void vote(Long id) {
        // 远程IP + doc.id 作为key，24小时内不能重复
        String ip = RequestContext.getRemoteAddr();
        if(redisUtil.validateRepeat("DOC_VOTE_" + id + "_" + ip, 3600 * 24)) {
            docMapperCust.increaseVoteCount(id);
        } else {
            throw new BusinessException(BusinessExceptionCode.VOTE_REPEAT);
        }

        // 推送消息
        Doc docDB = docMapper.selectByPrimaryKey(id);
        webSocketServer.sendInfo("【" + docDB.getName() + "】被点赞啦！");
    }

    /**
     * 定时更新内容（文档数、阅读数、点赞数）到电子书
     */
    public void updateEbookInfo() {
        docMapperCust.updateEbookInfo();
    }
}
