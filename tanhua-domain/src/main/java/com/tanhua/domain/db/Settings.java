package com.tanhua.domain.db;
import lombok.Data;

/**
 * 通知设置
 */
@Data
public class Settings extends BasePojo {
    private Long id;
    private Long userId;
    private Boolean likeNotification; // 推送喜欢我的信息
    private Boolean pinglunNotification;// 推送评论信息
    private Boolean gonggaoNotification;// 推送公告信息
}