package com.tanhua.dubbo.api;

import com.tanhua.domain.vo.PageResult;

public interface FriendApi {

    /**
     * 添加联系人
     * @param loginUserId
     * @param userId
     */
    void add(Long loginUserId, Long userId);

    /**
     * 联系人列表分页查询
     * @param userId
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    PageResult findPage(Long userId, Long page, Long pagesize, String keyword);
}
