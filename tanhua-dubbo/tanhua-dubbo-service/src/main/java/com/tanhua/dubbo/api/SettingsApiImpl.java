package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.SettingsMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/7
 */
@Service
public class SettingsApiImpl implements SettingsApi {

    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 查询用户通知设置
     * @param userId
     * @return
     */
    @Override
    public Settings findByUserId(Long userId) {
        // 构建查询条件
        QueryWrapper<Settings> queryWrapper = new QueryWrapper<Settings>();
        // where user_id=userId
        queryWrapper.eq("user_id",userId);
        // 只查询一条记录，如果有多条则报错
        return settingsMapper.selectOne(queryWrapper);
    }

    @Override
    public void update(Settings settings) {
        settingsMapper.updateById(settings);
    }

    /**
     * 添加通知设置
     *
     * @param settings
     */
    @Override
    public void save(Settings settings) {
        settingsMapper.insert(settings);
    }
}
