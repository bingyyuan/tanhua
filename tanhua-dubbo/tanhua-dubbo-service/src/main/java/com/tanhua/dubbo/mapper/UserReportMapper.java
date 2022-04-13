package com.tanhua.dubbo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.domain.db.UserAnswer;
import com.tanhua.domain.db.UserReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserReportMapper extends BaseMapper<UserReport> {
    /**
     * 查询类似用户的id
     * @param questionnaireId
     * @param conclusionID
     * @param userId
     * @return
     */
    List<Long> findSimilarUserIds(@Param("questionnaireId") Integer questionnaireId,
                                  @Param("conclusionID") Integer conclusionID,
                                  @Param("userId") Long userId);

    /**
     * 添加问卷报告结果
     * @param reportId
     * @param userId
     */
    void addReport(String reportId, Long userId);

    /**
     * 通过问题id与用户id查询现有的报告id，用于判断用户是否做过同一套问卷
     * @param userAnswer
     * @return
     */
    String findByAnswer(UserAnswer userAnswer);

    /**
     * 查询用户完成的最高等级
     * @param userId
     * @return
     */
    Integer findMaxLevelByUserId(Long userId);
}
