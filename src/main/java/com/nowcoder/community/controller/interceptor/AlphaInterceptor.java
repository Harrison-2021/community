package com.nowcoder.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Component
public class AlphaInterceptor implements HandlerInterceptor {
//    private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    // 在Controller之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        logger.debug("preHandle" + handler.toString());

        // 如果返回false，就不会再继续往下执行
        return true;
    }

    // 在Controller请求后，模板起作用时执行，即渲染前执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        logger.debug("postHandle" + handler.toString());
    }

    // 在TemplateEngine之后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        logger.debug("afterHandle" + handler.toString());
    }
}
