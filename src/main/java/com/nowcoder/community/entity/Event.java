package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    private String topic; // kafka服务器要识别的topic事件类型
    private int userId; // 触发事件的用户id
    private int entityType; // 触发事件的类型，帖子/评论，点赞，关注中的一种
    private int entityId;
    private int entityUserId; // 触发的事件的作者，有可能等于entityId，要分情况对待
    private Map<String, Object> data = new HashMap<>(); // 事件中传入的其他信息，统一包装成一个map对象存储起来

    public String getTopic() {
        return topic;
    }

    // 为了调用方便，将set的返回类型改为当前对象，以便可以连续调用set方法
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    // 对于map类型的数据，希望传入的是具体的key，value，以便设置
    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
