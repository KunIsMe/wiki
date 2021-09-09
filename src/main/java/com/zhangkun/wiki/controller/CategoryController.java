package com.zhangkun.wiki.controller;

import com.zhangkun.wiki.req.CategoryQueryReq;
import com.zhangkun.wiki.req.CategorySaveReq;
import com.zhangkun.wiki.resp.CategoryQueryResp;
import com.zhangkun.wiki.resp.CommonResp;
import com.zhangkun.wiki.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/all")
    public CommonResp all(CategoryQueryReq req) {
        CommonResp<List<CategoryQueryResp>> resp = new CommonResp<>();
        List<CategoryQueryResp> list = categoryService.all(req);
        resp.setContent(list);
        return resp;
    }

//    @GetMapping("/list")
//    public CommonResp list(@Valid CategoryQueryReq req) {
//        CommonResp<PageResp<CategoryQueryResp>> resp = new CommonResp<>();
//        PageResp<CategoryQueryResp> list = categoryService.list(req);
//        resp.setContent(list);
//        return resp;
//    }

    @PostMapping("/save")
    public CommonResp list(@Valid @RequestBody CategorySaveReq req) {
        CommonResp resp = new CommonResp<>();
        categoryService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp list(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>();
        categoryService.delete(id);
        return resp;
    }
}
