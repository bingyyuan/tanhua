package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.domain.db.Questionnaire;

import java.util.List;

public interface QuestionnaireMapper extends BaseMapper<Questionnaire> {
    /**
     * 查询所有问卷
     * @return
     */
    List<Questionnaire> findAll(Long loginUserId);
}
