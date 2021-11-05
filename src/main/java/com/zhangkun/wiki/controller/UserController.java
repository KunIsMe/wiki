package com.zhangkun.wiki.controller;

import com.zhangkun.wiki.req.UserQueryReq;
import com.zhangkun.wiki.req.UserSaveReq;
import com.zhangkun.wiki.resp.CommonResp;
import com.zhangkun.wiki.resp.UserQueryResp;
import com.zhangkun.wiki.resp.PageResp;
import com.zhangkun.wiki.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public CommonResp list(@Valid UserQueryReq req) {
        CommonResp<PageResp<UserQueryResp>> resp = new CommonResp<>();
        PageResp<UserQueryResp> list = userService.list(req);
        resp.setContent(list);
        return resp;
    }

    @PostMapping("/save")
    public CommonResp list(@Valid @RequestBody UserSaveReq req) {
        CommonResp resp = new CommonResp<>();
        userService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp list(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>();
        userService.delete(id);
        return resp;
    }
}
