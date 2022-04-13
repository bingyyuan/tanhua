package com.tanhua.server.service;

import com.tanhua.domain.db.Questionnaire;
import com.tanhua.domain.db.UserAnswer;
import com.tanhua.domain.vo.UserReportVo;
import com.tanhua.dubbo.api.TestSoulApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestSoulService {

    @Reference
    private TestSoulApi testSoulApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取问卷信息
     * @return
     */
    public List<Questionnaire> findAll() {
        // 获取当前登陆用户
        Long loginUserId = UserHolder.getUserId();
        // 查询所有问卷
        List<Questionnaire> list = testSoulApi.findAll(loginUserId);
        // 当前登陆用户可解锁的问卷等级
        String key = "test_soul_level_" + loginUserId;
        Integer level = (Integer) redisTemplate.opsForValue().get(key);
        if(null == level){
            // 如果用户没做过问卷，则解锁1级（初级）问卷
            level = 1;
            redisTemplate.opsForValue().set(key,level);
        }
        for (Questionnaire questionnaire : list) {
            // 根据级别，判断是否需要解锁
            if (questionnaire.getLv() <= level) {
                questionnaire.setIsLock(0);
            }
        }
        return list;
    }

    /**
     * 提交问卷答案
     * @param answers
     * @return
     */
    public String submit(List<UserAnswer> answers) {
        final Long userId = UserHolder.getUserId();
        // 设置当前提交的用户id
        answers.forEach(a->a.setUserId(userId));
        // 提交问卷
        String reportId = testSoulApi.submit(userId,answers);
        // 查询用户完成的最高等级
        Integer reportLevel = testSoulApi.findLevelByUserId(userId);
        // 设置解锁的等级
        String key = "test_soul_level_" + userId;
        // 解锁下一级
        redisTemplate.opsForValue().set(key,reportLevel+1);
        // 返回问卷报告id
        return reportId;
    }

    /**
     * 查询报告
     * @param reportId
     * @return
     */
    public UserReportVo findReportById(String reportId) {
        return testSoulApi.findReportById(reportId);
    }
}
