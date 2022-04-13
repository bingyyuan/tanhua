package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 用户的服务提供者
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/4
 */
@Service // Service 包名为apache.dubbo的
public class UserApiImpl implements UserApi {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public User findByMobile(String mobile) {
        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        // 按手机号码查询
        queryWrapper.eq("mobile",mobile);
        return userMapper.selectOne(queryWrapper);
    }
}
