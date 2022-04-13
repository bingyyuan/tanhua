package com.tanhua.domain.vo;
import lombok.Data;
import java.io.Serializable;

/**
 * 推荐好友信息
 */
@Data
public class TodayBestVo implements Serializable {
    private Long id; // 用户的id
    private String avatar; // 头像
    private String nickname;
    private String gender; //性别 man woman
    private Integer age;
    private String[] tags;
    private Long fateValue; //缘分值
}