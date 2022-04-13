package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Settings;

/**
 * 通知设置
 */
public interface SettingsApi {
    //根据用户id查询通知配置
    Settings findByUserId(Long userId);

    /**
     * 更新通知设置
     * @param settings
     */
    void update(Settings settings);

    /**
     * 添加通知设置
     * @param settings
     */
    void save(Settings settings);
}