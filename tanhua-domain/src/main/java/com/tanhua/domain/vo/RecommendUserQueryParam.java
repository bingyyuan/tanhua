package com.tanhua.domain.vo;
import lombok.Data;
import java.io.Serializable;

/**
 * 用户推荐的查询条件
 */
@Data
public class RecommendUserQueryParam implements Serializable {

    private Integer page;
    private Integer pagesize;
    private String gender; // 性别，man,women
    private String lastLogin; // 最后登陆时间
    private Integer age; // 年龄
    private String city; // 所的城市
    private String education; // 学历
}