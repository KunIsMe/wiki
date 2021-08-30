package com.zhangkun.wiki.controller;

import com.zhangkun.wiki.domain.Test;
import com.zhangkun.wiki.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/test/list")
    public List<Test> list() {
        return testService.list();
    }
}
