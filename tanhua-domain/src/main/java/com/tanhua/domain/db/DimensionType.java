package com.tanhua.domain.db;

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
@TableName("tb_dimension_type")
public class DimensionType implements Serializable {

    private Integer id;

    private String name;


}
