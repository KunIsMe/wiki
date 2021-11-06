package com.zhangkun.wiki.controller;

import com.alibaba.fastjson.JSONObject;
import com.zhangkun.wiki.req.UserLoginReq;
import com.zhangkun.wiki.req.UserQueryReq;
import com.zhangkun.wiki.req.UserResetPasswordReq;
import com.zhangkun.wiki.req.UserSaveReq;
import com.zhangkun.wiki.resp.CommonResp;
import com.zhangkun.wiki.resp.UserLoginResp;
import com.zhangkun.wiki.resp.UserQueryResp;
import com.zhangkun.wiki.resp.PageResp;
import com.zhangkun.wiki.service.UserService;
import com.zhangkun.wiki.util.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SnowFlake snowFlake;

    @GetMapping("/list")
    public CommonResp list(@Valid UserQueryReq req) {
        CommonResp<PageResp<UserQueryResp>> resp = new CommonResp<>();
        PageResp<UserQueryResp> list = userService.list(req);
        resp.setContent(list);
        return resp;
    }

    @PostMapping("/save")
    public CommonResp save(@Valid @RequestBody UserSaveReq req) {
        // md5 加密存储
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));

        CommonResp resp = new CommonResp<>();
        userService.save(req);
        return resp;
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp delete(@PathVariable Long id) {
        CommonResp resp = new CommonResp<>();
        userService.delete(id);
        return resp;
    }

    @PostMapping("/reset-password")
    public CommonResp resetPassword(@Valid @RequestBody UserResetPasswordReq req) {
        // md5 加密存储
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));

        CommonResp resp = new CommonResp<>();
        userService.resetPassword(req);
        return resp;
    }

    @PostMapping("/login")
    public CommonResp login(@Valid @RequestBody UserLoginReq req) {
        // md5 加密存储
        req.setPassword(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()));
        CommonResp<UserLoginResp> resp = new CommonResp<>();
        UserLoginResp userLoginResp = userService.login(req);

        // 生成单点登录token，并放入redis中
        Long token = snowFlake.nextId();
        userLoginResp.setToken(token.toString());
        redisTemplate.opsForValue().set(token, JSONObject.toJSONString(userLoginResp), 3600 * 24, TimeUnit.SECONDS);

        resp.setContent(userLoginResp);
        return resp;
    }
}
