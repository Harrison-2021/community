package com.nowcoder.community;

import com.nowcoder.community.dao.*;
import com.nowcoder.community.entity.*;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest implements CommunityConstant{
    @Autowired
    UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);
        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
        user = userMapper.selectByName("liubei");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for(DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(199);
        loginTicket.setTicket("test2");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket ticket = loginTicketMapper.selectByTicket("test");
        System.out.println(ticket);

        loginTicketMapper.updateLoginTicket("test", 1);
        ticket = loginTicketMapper.selectByTicket("test");
        System.out.println(ticket.getStatus());
    }

    @Test
    public void testInsertComment() {
        Comment comment = new Comment();
        comment.setUserId(150);
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        comment.setContent("12345");
        commentMapper.insertComment(comment);
    }

    @Test
    public void testSelectLetters() {
        int i = 0;
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for(Message message : list) {
            System.out.println((++i) + " : " + message);
        }

        System.out.println("111?????????????????? " + messageMapper.selectConversationCount(111));

        list = messageMapper.selectLetters("111_112", 0, 10);
        i = 0;
        for(Message message : list) {
            System.out.println((++i) + " : " + message);
        }

        System.out.println("111???112?????????????????? " + messageMapper.selectLetterCount("111_112"));

        System.out.println("111?????????????????? " + messageMapper.selectLetterUnreadCount(131, null));
        System.out.println("111_131?????????????????? " + messageMapper.selectLetterUnreadCount(131, "111_131"));

    }

    @Test
    public void testNotice() {
        System.out.println(messageMapper.selectLatestNotice(111, TOPIC_COMMENT));
        System.out.println(messageMapper.selectNoticeCount(111, TOPIC_FOLLOW));
        System.out.println(messageMapper.selectNoticeUnreadCount(111, TOPIC_LIKE));
        System.out.println(messageMapper.selectNoticeUnreadCount(111, null));
    }
}
