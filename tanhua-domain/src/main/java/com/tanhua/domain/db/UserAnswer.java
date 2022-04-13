package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("tb_user_answer")
public class UserAnswer implements Serializable {

    @TableField("userId")
    private Long userId;

    @TableField("questionId")
    private Integer questionId;
    @TableField("optionId")
    private Integer optionId;

    @TableField("reportId")
    private String reportId;


}
