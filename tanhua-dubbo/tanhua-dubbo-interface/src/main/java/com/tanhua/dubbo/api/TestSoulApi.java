package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Questionnaire;
import com.tanhua.domain.db.UserAnswer;
import com.tanhua.domain.vo.UserReportVo;

import java.util.List;

public interface TestSoulApi {
    List<Questionnaire> findAll(Long loginUserId);

    String submit(Long userId,List<UserAnswer> answers);

    UserReportVo findReportById(String reportId);

    /**
     * 查询用户完成的最高等级
     * @param userId
     * @return
     */
    Integer findLevelByUserId(Long userId);
}
