package com.tanhua.domain.vo;

import lombok.Data;

/**
 * <p>
 * VO: 代表传输数据，一般用于前端传过来的数据封装，或返回给前端的封装
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/6
 */
@Data
public class UserInfoVo {
    private Long id; //用户id
    private String nickname; //昵称
    private String avatar; //用户头像
    private String birthday; //生日
    private String gender; //性别
    private String age; //年龄
    private String city; //城市
    private String income; //收入
    private String education; //学历
    private String profession; //行业
    private Integer marriage; //婚姻状态
}
