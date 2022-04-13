package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* <p>
    * 
    * </p>
*
* @author itheima
* @since 2021-03-26
*/
    @Data
        @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    @TableName("tb_question")
    public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

            /**
            * 用户id
            */
    private Long userId;

            /**
            * 问题内容
            */
    private String txt;

    private LocalDateTime created;

    private LocalDateTime updated;


}
