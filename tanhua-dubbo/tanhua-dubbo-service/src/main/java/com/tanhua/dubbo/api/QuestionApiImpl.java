package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Question;
import com.tanhua.dubbo.mapper.QuestionMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/7
 */
@Service
public class QuestionApiImpl implements QuestionApi {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 通过用户id查询陌生人问题
     * @param userId
     * @return
     */
    @Override
    public Question findByUserId(Long userId) {
        // 构建查询条件
        QueryWrapper<Question> queryWrapper = new QueryWrapper<Question>();
        // where user_id=userId
        queryWrapper.eq("user_id",userId);
        // 只查询一条记录，如果有多条则报错
        return questionMapper.selectOne(queryWrapper);
    }

    /**
     * 添加陌生人问题
     * @param question
     */
    @Override
    public void save(Question question) {
        questionMapper.insert(question);
    }

    /**
     * 更新陌生人问题
     *
     * @param question
     */
    @Override
    public void update(Question question) {
        questionMapper.updateById(question);
    }
}
