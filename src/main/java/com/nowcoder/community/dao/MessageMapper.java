package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    // 查询当前用户的会话列表，注意，针对每个会话只返回一个最新的私信，也就是id最大的会话，故要用到组查询
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表-二级查询
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量，注意，分两者情况，当前用户的私信，以及某个会化的私信，要在sql中动态拼接
    // 私信的逻辑是，当前用户信息显示所有与之相关的私信总和，列表中的每个会话中显示当前会话用户与当前用户之间的所有私信总和
    int selectLetterUnreadCount(int userId, String conversationId);

    // 新增消息
    int insertMessage(Message message);

    // 修改消息的状态
    int updateMessage(List<Integer> ids, int status);

    // 查询系统发送给特定主题的最新一条消息
    int selectTopicMessage(int userId, String topic);

    // 查询系统发送给特定主题的消息数量
    int selectTopicCount(int userId, String topic);

    // 查询系统发送给特定主题的未读消息数量
    int selectTopicUnreadCount(int userId, String topic);
}
