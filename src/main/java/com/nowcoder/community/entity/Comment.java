package com.nowcoder.community.entity;

import java.util.Date;

public class Comment {
    private int id;     // 评论id
    private int userId;  // 评论作者
    private int entityType;  // 评论的级别，一级为回复帖子的评论，二级为回复评论的评论
    private int entityId;    // 评论所属实体类的id，一级评论为帖子的id,即传入comment.getEntityId()，二级评论为一级评论的id,即传入comment.getId()
    private int targetId;    // 方便统计二级评论设定的目标用户id，一级和二级直接回复一级评论的默认都为0，目标是帖子用户，二级的是指定回复的用户id
    private String content;  // 评论的内容，要注意过滤html标签和敏感词
    private int status;      // 状态，0，为有效，1为无效
    private Date createTime; // 创建时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", targetId=" + targetId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
