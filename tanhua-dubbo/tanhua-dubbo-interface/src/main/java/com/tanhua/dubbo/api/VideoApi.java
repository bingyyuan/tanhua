package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

/**
 * 小视频服务接口
 */
public interface VideoApi {
    /**
     * 发布小视频
     * @param video
     */
    void save(Video video);

    /**
     * 小视频分页查询
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPage(Long page, Long pagesize);

    /**
     * 关注
     * @param followUser
     */
    void followUser(FollowUser followUser);

    /**
     * 取消关注
     * @param followUser
     */
    void unfollowUser(FollowUser followUser);
}
