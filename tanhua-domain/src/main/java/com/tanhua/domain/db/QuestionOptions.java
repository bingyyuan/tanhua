package com.tanhua.domain.db;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author itheima
 * @since 2021-03-26
 */
@Data
@TableName("tb_question_options")
public class QuestionOptions implements Serializable {

    private String id;

    private String option;
    @JsonIgnore
    private Integer score;
    @JsonIgnore
    @TableField("questionId")
    private Integer questionId;
    @JsonIgnore
    private Integer sort;
}
