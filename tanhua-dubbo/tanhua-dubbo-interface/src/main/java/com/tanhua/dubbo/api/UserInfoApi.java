package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/6
 */
public interface UserInfoApi {
    /**
     * 添加用户详情信息
     * @param userInfo
     */
    void save(UserInfo userInfo);

    /**
     * 更新用户信息
     */
    void update(UserInfo userInfo);

    /**
     * 通过id查询用户详情
     * @param id
     * @return
     */
    UserInfo findById(Long id);


    /**
     * 通过用户ids集合查询用户信息
     * @param userIds
     * @return
     */
    List<UserInfo> findByIds(List<Long> userIds);
}
