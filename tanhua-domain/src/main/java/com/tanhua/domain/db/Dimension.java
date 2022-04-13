package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("tb_dimension_type")
public class Dimension implements Serializable {
    private Integer id;
    private String name;
    @TableField(exist = false)
    private String userDimensionValue;
}
