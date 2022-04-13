package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 * 用户信息服务
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/6
 */
@Service
public class UserInfoApiImpl implements UserInfoApi {
    @Autowired
    private UserInfoMapper userInfoMapper;
    /**
     * 添加用户详情信息
     *
     * @param userInfo
     */
    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    /**
     * 更新用户信息
     *
     * @param userInfo
     */
    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 通过id查询用户详情
     * @param id
     * @return
     */
    @Override
    public UserInfo findById(Long id) {
        //userInfoMapper.selectBatchIds();// 查询多个id, in (id1,id2,id3)
        return userInfoMapper.selectById(id);
    }

    /**
     * 通过用户ids集合查询用户信息
     * @param userIds
     * @return
     */
    @Override
    public List<UserInfo> findByIds(List<Long> userIds) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<UserInfo>();
        queryWrapper.in("id",userIds);
        return userInfoMapper.selectList(queryWrapper);
    }
}
