package com.tanhua.domain.db;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author itheima
 * @since 2021-03-26
 */
@Data
@TableName("tb_questionnaire")
public class Questionnaire implements Serializable {

    private String id;
    private String name;
    private String cover;
    @JsonIgnore
    private Integer lv; // 级别
    @TableField(exist = false)
    private String level; // 级别名称
    private Integer star;
    @TableField(exist = false)
    private List<SoulQuestion> questions;
    @TableField(exist = false)
    private Integer isLock=1;
    @TableField(exist = false)
    private String reportId;

}
