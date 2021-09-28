package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisLikeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisLikeUtil.getEntityLikeKey(entityType, entityId);

        // 要先判断是否已经点过赞了，如果点过了，处理逻辑是取消点赞，不是add，而是remove
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

        if(isMember) {
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }
    }
    // 查询某实体点赞数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisLikeUtil.getEntityLikeKey(entityType, entityId);

        return redisTemplate.opsForSet().size(entityLikeKey);
    }
    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int entityType, int entityId, int userId) {
        String entityLikeKey = RedisLikeUtil.getEntityLikeKey(entityType, entityId);

        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
