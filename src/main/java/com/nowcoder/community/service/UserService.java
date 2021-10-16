package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 查询用户时，首先从缓存中查询
    public User findUserById(int id) {
        return userMapper.selectById(id);
//        User user = getCache(id);
//        if(user == null) {
//            user = setCache(id);
//        }
//        return user;
    }

    /** 处理注册业务 */
    // 注意，判断错误信息后，一定要记得返回，终止操作
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if(user == null) {
            throw new IllegalArgumentException("参数不能为空!"); // 代码逻辑错误
        }
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null) {
            map.put("usernameMsg", "该账号已存在!"); // msg信息覆盖
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null) {
            map.put("emailMsg", "该邮箱已存在!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.genereateUUID().substring(0, 5));
        // 注意密码明文与salt的顺序千万不要弄反了，登录时候验证密码时候顺序必须保持一致
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.genereateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮箱
        Context context = new Context(); // 设置邮箱要发送的内容
        context.setVariable("email", user.getEmail());
        // 拼url地址，http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // 模板引擎自动识别context传入的参数，然后动态加载到网页中，将网页的动态变量进行替换，并将网页内容加载到content中
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    /** 激活账户，激活码正确，将用户状态改为已激活状态为1 */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            // 修改用户状态，需要删除缓存
            userMapper.updateStatus(userId, 1);
//            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /** 处理登录业务*/
    // 只处理与数据库相关内容
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if(user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 验证状态
        if(user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 生成登录凭证
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setTicket(CommunityUtil.genereateUUID());
        ticket.setStatus(0); // 0有效，1无效，登出时设置为1
        // 注意日期转换的格式
        ticket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        loginTicketMapper.insertLoginTicket(ticket);
        // 改成用redis存储，可以直接存储一个对象，redis会自动转换为json格式的字符串
//        String ticketKey = RedisKeyUtil.getTicketKey(ticket.getTicket());
//        redisTemplate.opsForValue().set(ticketKey, ticket);

        map.put("ticket", ticket.getTicket()); // 将ticket返回给服务器，服务器回复给浏览器保存

        return map;
    }

    /** 登出业务处理*/
    public void logout(String ticket) {
        loginTicketMapper.updateLoginTicket(ticket, 1);
        // redis获取t票，改变状态，再存储进去
//        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
//        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
//        loginTicket.setStatus(1);
//        redisTemplate.opsForValue().set(ticketKey, ticket);
    }

    /** 查询t票业务*/
    public LoginTicket selectByTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
//        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
//        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    /**更新用户的头像 */
    // 因为没有将redis缓存与用户调用mysql数据库包装成一个事务，需要先更新数据库，更新成功后再清理缓存
    public int updateUserHeaderUrl(int userId, String headerUrl) {
        int row = userMapper.updateHeader(userId, headerUrl);
//        clearCache(userId);
        return row;
    }

    public User findUserByName(String name) {
        return userMapper.selectByName(name);
    }

    /** 使用redis缓冲用户数据，优先调用redis对用户信息进行访问*/
//    // 1. 优先从缓冲中取数据
//    private User getCache(int userId) {
//        String userKey = RedisKeyUtil.getUser(userId);
//        return (User) redisTemplate.opsForValue().get(userKey);
//    }
//
//    // 2. 取不到时候，初始化缓存数据
//    private User setCache(int userId) {
//        User user = userMapper.selectById(userId);
//        String userKey = RedisKeyUtil.getUser(userId);
//        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
//        return user;
//    }
//
//    // 3. 数据变更时清除缓存数据
//    private void clearCache(int userId) {
//        String userKey = RedisKeyUtil.getUser(userId);
//        redisTemplate.delete(userKey);
//    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
