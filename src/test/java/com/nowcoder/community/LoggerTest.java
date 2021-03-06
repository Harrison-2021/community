package com.nowcoder.community;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    public void testLogger() {
//        System.out.println(logger.getName());
//        System.out.println(CommunityUtil.md5("xx1" + "c6c94"));
        // new Date()传入的参数是long型，且System.currentTimeMillis()是毫秒
//        System.out.println(new Date(System.currentTimeMillis() + CommunityConstant.REMEMBER_EXPIRED_SECONDS*1000L));
//        String s1 = new String("aaa");
//        String s2 = new String("aaa");
//        System.out.println(s1 == s2);
//
//        String s3 = new String("2");
//        s3.intern();
//        String s4 = "2";
//        System.out.println(s3 == s4);

        String s5 = new String("3") + new String("3");
        s5.intern();
        String s6 = "33";
        System.out.println(s5 == s6);

//        logger.debug("debug log");
//        logger.info("info log");
//        logger.warn("warn log");
//        logger.error("error log");


    }
}
