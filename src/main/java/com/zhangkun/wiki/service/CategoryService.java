package com.zhangkun.wiki.service;

import com.zhangkun.wiki.domain.Category;
import com.zhangkun.wiki.domain.CategoryExample;
import com.zhangkun.wiki.mapper.CategoryMapper;
import com.zhangkun.wiki.req.CategoryQueryReq;
import com.zhangkun.wiki.req.CategorySaveReq;
import com.zhangkun.wiki.resp.CategoryQueryResp;
import com.zhangkun.wiki.util.CopyUtil;
import com.zhangkun.wiki.util.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SnowFlake snowFlake;

    /**
     * 分类列表请求（不分页）
     * @param req
     * @return
     */
    public List<CategoryQueryResp> all(CategoryQueryReq req) {
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.setOrderByClause("sort asc");
        CategoryExample.Criteria criteria = categoryExample.createCriteria();
        if(!ObjectUtils.isEmpty(req.getName())) {
            criteria.andNameLike("%" + req.getName() + "%");
        }
        List<Category> categoryList = categoryMapper.selectByExample(categoryExample);

        // 列表复制
        List<CategoryQueryResp> categoryRespList = CopyUtil.copyList(categoryList, CategoryQueryResp.class);

        return categoryRespList;
    }

    /**
     * 分类列表请求
     * @param req
     * @return
     */
//    public PageResp<CategoryQueryResp> list(CategoryQueryReq req) {
//        CategoryExample categoryExample = new CategoryExample();
//        categoryExample.setOrderByClause("sort asc");
//        CategoryExample.Criteria criteria = categoryExample.createCriteria();
//        if(!ObjectUtils.isEmpty(req.getName())) {
//            criteria.andNameLike("%" + req.getName() + "%");
//        }
//        PageHelper.startPage(req.getPage(), req.getSize());
//        List<Category> categoryList = categoryMapper.selectByExample(categoryExample);
//
//        PageInfo<Category> pageInfo = new PageInfo<>(categoryList);
//        System.out.println("总行数：" + pageInfo.getTotal());
//        System.out.println("总页数：" + pageInfo.getPages());
//
//        // 列表复制
//        List<CategoryQueryResp> categoryRespList = CopyUtil.copyList(categoryList, CategoryQueryResp.class);
//
//        PageResp<CategoryQueryResp> pageResp = new PageResp<>();
//        pageResp.setTotal(pageInfo.getTotal());
//        pageResp.setList(categoryRespList);
//
//        return pageResp;
//    }

    /**
     * 分类编辑或增加
     * @param req
     */
    public void save(CategorySaveReq req) {
        Category category = CopyUtil.copy(req, Category.class);
        if(ObjectUtils.isEmpty(req.getId())){
            // 新增电子书
            category.setId(snowFlake.nextId());
            categoryMapper.insert(category);
        } else {
            // 编辑电子书
            categoryMapper.updateByPrimaryKey(category);
        }
    }

    /**
     * 分类删除
     * @param id
     */
    public void delete(Long id) {
        categoryMapper.deleteByPrimaryKey(id);
    }
}
