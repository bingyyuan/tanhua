package com.tanhua.dubbo.api;

import com.tanhua.domain.vo.PageResult;

public interface UserLikeApi {
    /**
     * 互相喜欢数量
     * @param loginUserId
     * @return
     */
    Long countLikeEachOther(Long loginUserId);

    /**
     * 我喜欢数量
     * @return
     */
    Long countOneSideLike(Long loginUserId);

    /**
     * 喜欢我的人数
     * @return
     */
    Long countFens(Long loginUserId);

    /**
     * 相互喜欢列表
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageLikeEachOther(Long loginUserId, Long page, Long pagesize);

    /**
     * 我喜欢的列表
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageOneSideLike(Long loginUserId, Long page, Long pagesize);

    /**
     * 我的粉丝列表
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageFens(Long loginUserId, Long page, Long pagesize);

    /**
     * 访客列表
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPageMyVisitors(Long loginUserId, Long page, Long pagesize);

    /**
     * 登陆用户喜欢粉丝
     * @param loginUserId
     * @param fansId
     */
    boolean fansLike(Long loginUserId, Long fansId);

    /**
     * 添加喜欢
     * @param userId
     * @param targetUserId
     */
    void likeUser(Long userId, Long targetUserId);

    /**
     * 取消喜欢
     * @param userId
     * @param targetUserId
     */
    boolean unLikeUser(Long userId, Long targetUserId);

    /**
     * 判断登陆用户是否已喜欢
     * @param loginUserId
     * @param userId
     * @return
     */
    boolean alreadyLove(Long loginUserId, Long userId);
}
