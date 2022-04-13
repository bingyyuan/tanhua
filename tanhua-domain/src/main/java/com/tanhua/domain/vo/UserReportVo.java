package com.tanhua.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author itheima
 * @since 2021-03-26
 */
@Data
public class UserReportVo implements Serializable {

    private String conclusion;
    private String cover;

    private List<Map<String,String>> dimensions;
    private List<Map<String,Object>> similarYou = Collections.emptyList();

}
