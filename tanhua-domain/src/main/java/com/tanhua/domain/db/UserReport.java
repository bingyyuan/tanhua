package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tb_user_report")
public class UserReport implements Serializable {

    private String id;
    @TableField("questionnaireId")
    private Integer questionnaireId;
    @TableField("userId")
    private Long userId;
    private Date created;
    @TableField("conclusionID")
    private Integer conclusionID;
}
