package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/9
 */
public interface RecommendUserApi {
    /**
     * 查询佳人，缘分值最高的
     * @param loginUserId
     * @return
     */
    RecommendUser queryWithMaxScore(Long loginUserId);

    /**
     * 首页推荐用户分页查询
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult findPage(Integer page, Integer pagesize, Long userId);

    /**
     * 查询推荐用户的缘分值
     * @param userId 登陆用户
     * @param id 佳人id
     * @return
     */
    Double queryForScore(Long userId, Long id);
}
