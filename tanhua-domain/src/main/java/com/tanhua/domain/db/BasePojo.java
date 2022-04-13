package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/6
 */
@Data
public abstract class BasePojo implements Serializable {
    // fill 自动填充 什么时候触发 INSERT时
    @TableField(fill = FieldFill.INSERT)
    private Date created;
    // 什么时候触发 INSERT_UPDATE时
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updated;
}
