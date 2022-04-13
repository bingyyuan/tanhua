package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/7
 */
public interface BlackListApi {
    /**
     * 分页查询
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult findBlackList(long page, long pagesize, Long userId);

    /**
     * 移除黑名单
     * @param userId
     * @param uid
     */
    void delete(Long userId, long uid);
}
