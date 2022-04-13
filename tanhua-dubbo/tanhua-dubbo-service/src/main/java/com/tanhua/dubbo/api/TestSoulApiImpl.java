package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tanhua.domain.db.*;
import com.tanhua.domain.vo.UserReportVo;
import com.tanhua.dubbo.mapper.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestSoulApiImpl implements TestSoulApi {
    @Autowired
    private QuestionnaireMapper questionnaireMapper;

    @Autowired
    private UserAnswerMapper answerMapper;

    @Autowired
    private ConclusionMapper conclusionMapper;

    @Autowired
    private DimensionMapper dimensionMapper;

    @Autowired
    private UserReportMapper userReportMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 查询所有问卷
     * @return
     */
    @Override
    public List<Questionnaire> findAll(Long loginUserId) {
        return questionnaireMapper.findAll(loginUserId);
    }


    /**
     * 提交问卷答案
     * @param userId
     * @param answers
     * @return
     */
    @Override
    @Transactional
    public String submit(Long userId,List<UserAnswer> answers) {
        // 如果没答案，则不处理
        if(CollectionUtils.isEmpty(answers)){
            return null;
        }
        // 是否重做，如果是重做则删除旧数据
        // 通过问题id与用户id查询现有的报告id，用于判断用户是否做过同一套问卷
        String reportIdIDb = userReportMapper.findByAnswer(answers.get(0));
        if(StringUtils.isNotEmpty(reportIdIDb)){
            // 删除旧答案
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("reportId",reportIdIDb);
            answerMapper.deleteByMap(map);
            // 删除旧的维度值
            dimensionMapper.deleteReportDimension(reportIdIDb);
            // 删除旧的报告
            userReportMapper.deleteById(reportIdIDb);
        }

        // 生成报告ID
        Long id = IdWorker.getId();
        final String reportId = id.toString();
        // 保存每个答案
        answers.forEach(a->{
            a.setReportId(reportId);
            answerMapper.insert(a);
        });

        // 统计维度值
        dimensionMapper.addReportDimension(reportId);
        // 添加报告信息
        userReportMapper.addReport(reportId,userId);
        return reportId;
    }

    /**
     * 查看报告
     * @param reportId
     * @return
     */
    @Override
    public UserReportVo findReportById(String reportId) {
        // 查询报告信息
        UserReport userReport = userReportMapper.selectById(reportId);
        // 查询结论信息
        Conclusion conclusion = conclusionMapper.selectById(userReport.getConclusionID());
        UserReportVo vo = new UserReportVo();
        vo.setConclusion(conclusion.getDescription());
        vo.setCover(conclusion.getCover());
        // 查询维度值
        List<Map<String,String>> dimensions = dimensionMapper.findByReportId(reportId);
        vo.setDimensions(dimensions);
        // 查询类似用户
        List<Long> userIds = userReportMapper.findSimilarUserIds(userReport.getQuestionnaireId(),userReport.getConclusionID(),userReport.getUserId());
        if(CollectionUtils.isNotEmpty(userIds)){
            // 批量查询用户信息
            List<UserInfo> userInfos = userInfoMapper.selectBatchIds(userIds);
            // 根据前端返回的数据要求，转成相应格式的数据
            List<Map<String, Object>> similarList = userInfos.stream().map(userInfo -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", userInfo.getId().intValue());
                map.put("avatar", userInfo.getAvatar());
                return map;
            }).collect(Collectors.toList());
            vo.setSimilarYou(similarList);
        }
        return vo;
    }

    /**
     * 查询用户完成的最高等级
     * @param userId
     * @return
     */
    @Override
    public Integer findLevelByUserId(Long userId) {
        return userReportMapper.findMaxLevelByUserId(userId);
    }
}
