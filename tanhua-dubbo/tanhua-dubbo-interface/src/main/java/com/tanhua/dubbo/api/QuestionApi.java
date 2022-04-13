package com.tanhua.dubbo.api;
import com.tanhua.domain.db.Question;

/**
 * 陌生人问题
 */
public interface QuestionApi {
    //根据用户id查询通知配置
    Question findByUserId(Long userId);

    /**
     * 添加陌生人问题
     * @param question
     */
    void save(Question question);

    /**
     * 更新陌生人问题
     * @param question
     */
    void update(Question question);
}