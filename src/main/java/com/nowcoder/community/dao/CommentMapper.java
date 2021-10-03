package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    // 查询具体实体类型，具体实体id，指定页码的评论集合
    // 回复帖子的评论类型是1, 回复评论的评论类型是2，具体的id是具体帖子的id号以及评论数据库的id号
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查找指定类型的所有评论数
    int selectCountByEntity(int entityType, int entityId);

    // 增加帖子
    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
