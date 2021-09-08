package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaService {
    public AlphaService() {
        System.out.println("实例化构造AlphaService");
    }

    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("销毁AlphaService");
    }

    // 测试项目整个依赖注入流程
    @Autowired
    private AlphaDAO alphaDAO;

    public String find() {
        return alphaDAO.select();
    }
}
