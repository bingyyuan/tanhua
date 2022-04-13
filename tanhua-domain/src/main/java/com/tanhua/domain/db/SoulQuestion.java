package com.tanhua.domain.db;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@TableName("tb_soul_question")
public class SoulQuestion implements Serializable {
    private String id;
    private String question;
    @JsonIgnore
    private Integer dimensionTypeId;
    @JsonIgnore
    private Integer questionnaireId;
    private List<QuestionOptions> options;
}
