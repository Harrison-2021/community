package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 查询指定行数
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.

    // 查询表中所有行数，如何userId为0，查询所有行数，如果不为零，查询指向用户id的所有行数
    int selectDiscussPostRows(@Param("userId") int userId);

    // 插入一行数据
    int insertDiscussPostRows(DiscussPost discussPost);

    // 查询指定帖子id的一行数据
    DiscussPost selectDiscussPostById(int id);

}
