package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussController implements CommunityConstant{
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    // 处理添加发布帖子请求，与JSON交互，不用刷新网页，只是向网页增添信息
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!请登录后再发布!");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPostRows(discussPost);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);


        // 报错情况，将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    // 查询指定贴子的请求，网页显示特定帖子信息，并同用户信息一块显示出来
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId,
                                 Model model, Page page) {
        // 获取帖子信息
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者，即获取帖子的用户信息，一并显示出来
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 帖子赞的数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount", likeCount);
        // 帖子赞的状态,0表示未点赞，1为点赞，未登录状态时，一定显示未点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :likeService.findEntityLikeStatus(ENTITY_TYPE_POST, post.getId(), hostHolder.getUser().getId());
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount()); // 可以从帖子数据库中获取评一级评论数，免得再次统计，提高效率

        // 评论：给指定帖子一级的所有评论
        // 回复：给评论的评论，不分级别，平行关系，只有目标评论和当前评论，即本项目评论只设置两级
        // 评论列表
        // 用list数据结构可以重复，可以重复的动态数组，每个位置放一个键值对，userId-targetId
        // 一级评论帖子，目标都为0，即所有一级评论平行展开，但二级帖子相互关联，因此两重循环可以实现展现所有帖子信息
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论组合列表容器，即所有评论加用户名，用户头像信息模块
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null) {
            // 遍历拿到每个评论，然后和用户信息组合成map，加入list中
            for (Comment comment : commentList) {
                // 一级评论组合容器map
                Map<String, Object> commentVoMap = new HashMap<>();
                // 评论、拿到一级评论信息
                commentVoMap.put("comment", comment);
                // 作者、拿到一级评论信息对应的用户名和用户头像信息
                commentVoMap.put("user", userService.findUserById(comment.getUserId())); // id要对应好，不要错了
                // 帖子赞的数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVoMap.put("likeCount", likeCount);
                // 帖子赞的状态,0表示未点赞，1为点赞，未登录状态时，一定显示未点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :likeService.findEntityLikeStatus(ENTITY_TYPE_COMMENT, comment.getId(), hostHolder.getUser().getId());
                commentVoMap.put("likeStatus", likeStatus);

                // 回复列表
                // 因为每个一级评论可能有对应的回复信息，将所有与当前一级评论相关的回复显示出来
                List<Comment> replyList = commentService.findCommentsByEntity(
                    ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE
                );
                // 回复的voMap组合列表，同样底层用动态数组实现的可重复集合，每个位置放键值对，当前用户与回复的用户，实现所有二级回复帖子的处理
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null) {
                    for(Comment reply : replyList) {
                        Map<String, Object> replyVoMap = new HashMap<>();
                        // 回复信息
                        replyVoMap.put("reply", reply);
                        // 当前作者
                        replyVoMap.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标作者
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVoMap.put("target", target);

                        // 帖子赞的数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVoMap.put("likeCount", likeCount);
                        // 帖子赞的状态,0表示未点赞，1为点赞，未登录状态时，一定显示未点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :likeService.findEntityLikeStatus(ENTITY_TYPE_COMMENT, reply.getId(), hostHolder.getUser().getId());
                        replyVoMap.put("likeStatus", likeStatus);


                        replyVoList.add(replyVoMap);
                    }

                }
                commentVoMap.put("replys", replyVoList);

                // 帖子的回复数量统计
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVoMap.put("replyCount", replyCount);

                commentVoList.add(commentVoMap);
            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }


}
