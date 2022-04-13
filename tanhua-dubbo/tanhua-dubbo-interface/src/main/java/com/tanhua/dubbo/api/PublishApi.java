package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/10
 */
public interface PublishApi {

    /**
     * 发布动态
     * @param publish
     */
    void add(Publish publish);

    /**
     * 通过用户id分页查询好友动态
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findFriendPublishByTimeline(Long page, Long pagesize,Long userId);

    /**
     * 查询推荐动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult findRecommendPublish(Long page, Long pagesize, Long userId);

    /**
     * 查询我的动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult findMyAlbum(Long page, Long pagesize, Long userId);

    /**
     * 通过id查询动态信息
     * @param publishId
     * @return
     */
    Publish findById(String publishId);

    /**
     * 更新动态的状态
     * @param publish
     */
    void updateState(Publish publish);
}
