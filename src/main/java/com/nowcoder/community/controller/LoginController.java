package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    UserService userService;

    @Autowired
    Producer kaptchaProducer;

    @Value("server.servlet.context-path")
    String contextPath;

    @Autowired
    RedisTemplate redisTemplate;

    /** 注册页面显示*/
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /** 登录页面显示*/
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /** 注册页面请求*/
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user); // 接收用户注册业务层反馈的信息，用户注册业务也已经启动
        // 如果注册成功没有错误，就已经向用户邮箱发送激活邮件，要在中间页面将提示显示出来，并8秒后自动跳转到主页
        if(map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，已经向您的邮箱发送了一封激活邮件，请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    /** 激活码跳转页面显示*/
    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /** 生成验证码图片网页显示*/
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session中
        //session.setAttribute("kaptcha", text);

        // 验证码的归属,生成一个随机字符串放入用户登录页面的cookie中
        String kaptchaOwer = CommunityUtil.genereateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwer);
        cookie.setMaxAge(120);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        // 将验证码存入redis中
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwer);
        // 可以直接设置有效时间
        redisTemplate.opsForValue().set(redisKey, text, 120, TimeUnit.SECONDS);

        // 将服务器中生成的图片直接输出写给浏览器
        response.setContentType("image/png");
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    /** 登录页面请求*/
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code,
                        @RequestParam(value = "rememberMe", defaultValue = "false") Boolean rememberMe,
                        HttpServletResponse response,
                       /* HttpSession session*/
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 先判断验证码,从session中获取
        //String kaptcha = (String)session.getAttribute("kaptcha");
        // 改成从redis中获取验证码
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)) {
            String rediskey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(rediskey);
        }


        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 检查账号，密码, 注意登录有效时间,如果验证无误，登录成功，跳转主页前要将ticket传给浏览器的cookie保存
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")) { // map中有ticket，说明登录信息验证成功，并下发了ticket，且在数据库中有备份
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            // 注意cookie是以秒为单位计时间的，但java中的Date类是以毫秒为单位计时间的
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /** 登出页面请求*/
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        // 返回的页面不能弄错了，重定向时，默认get请求
        return "redirect:/login";
    }

}

