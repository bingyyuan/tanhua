package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.NearUserVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.RecommendUserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.UserLikeApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.common.utils.Page;
import org.apache.dubbo.config.annotation.Reference;
import org.joda.time.LocalDate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 好友推荐业务
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/9
 */
@Service
@Slf4j
public class TodayBestService {

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private UserLikeApi userLikeApi;

    /**
     * 查询佳人，缘分值最高的
     * @return
     */
    public TodayBestVo queryTodayBest() {
        // 1. 获取登陆用户的id
        Long loginUserId = UserHolder.getUserId();
        log.info("今日佳人查询：登陆用户的id={}", loginUserId);
        // 2. 调用recommendUserApi查询佳人，缘分值最高的.
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(loginUserId);
        log.info("查询到的佳人：{}", recommendUser);
        // 3. 没值
        if(null == recommendUser) {
            // 给一个默认用户id (1)
            recommendUser = new RecommendUser();
            recommendUser.setScore(95d);
            recommendUser.setUserId(5l);
        }
        // 4. 有值
        // 5. 调用userInfoApi 通过id查询用户信息
        UserInfo userInfo = userInfoApi.findById(recommendUser.getUserId());
        log.info("查询到的用户信息: {}",userInfo);
        // 6. 构建vo，把查询佳人, 及用户信息转成vo
        TodayBestVo todayBestVo = new TodayBestVo();
        BeanUtils.copyProperties(userInfo,todayBestVo);
        // tags数组处理
        //todayBestVo.setTags(userInfo.getTags()==null?new String[]{}:userInfo.getTags().split(","));
        // 【注意】：尽量不要出现在空值
        todayBestVo.setTags(StringUtils.split(userInfo.getTags(),","));
        // 缘分值,向下取整
        todayBestVo.setFateValue(recommendUser.getScore().longValue());
        // 7. 返回给controller
        return todayBestVo;
    }

    /**
     * 首页推荐用户
     * @param param
     * @return
     */
    public PageResult<TodayBestVo> recommendList(RecommendUserQueryParam param) {
        // 先获取登陆用户的id
        Long userId = UserHolder.getUserId();
        // 调用recommendUserApi 分页查询推荐用户
        PageResult pageResult = recommendUserApi.findPage(param.getPage(),param.getPagesize(),userId);
        // 获取分页的结果集
        List<RecommendUser> items = (List<RecommendUser>)pageResult.getItems();
        // 判断结果集是否为空
        if(CollectionUtils.isEmpty(items)) {
            // 如果为空，没有推荐用户列表
            //    构建一些给他
            items = defaultRecommend();
        }
        // 通过用户的id查询用户详情
        // 遍历用户集合 每个查询下他们的详情信息
        // 变成List<todayBestVo> list
        List<TodayBestVo> list = new ArrayList<TodayBestVo>();
        for (RecommendUser item : items) {
            Long uid = item.getUserId();
            UserInfo userInfo = userInfoApi.findById(uid);
            TodayBestVo vo = new TodayBestVo();
            BeanUtils.copyProperties(userInfo,vo);
            // tags数组处理
            //todayBestVo.setTags(userInfo.getTags()==null?new String[]{}:userInfo.getTags().split(","));
            // 【注意】：尽量不要出现在空值
            vo.setTags(StringUtils.split(userInfo.getTags(),","));
            // 缘分值,向下取整
            vo.setFateValue(item.getScore().longValue());
            list.add(vo);
        }
        // 设置回pageResult，再返回
        pageResult.setItems(list);
        return pageResult;
    }


    //构造默认推荐数据
    private List<RecommendUser> defaultRecommend() {
        String ids = "1,2,3,4,5,6,7,8,9,10";
        List<RecommendUser> records = new ArrayList<>();
        for (String id : ids.split(",")) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(Long.valueOf(id));
            recommendUser.setScore(RandomUtils.nextDouble(70, 86));
            records.add(recommendUser);
        }
        return records;
    }

    /**
     * 佳人信息
     * @param id
     * @return
     */
    public TodayBestVo personalInfo(Long id) {
        // 查询用户信息
        UserInfo userInfo = userInfoApi.findById(id);
        // 查询推荐表中的用户信息，获取缘分值
        Double score = recommendUserApi.queryForScore(UserHolder.getUserId(), id);
        // 构建vo
        TodayBestVo vo = new TodayBestVo();
        BeanUtils.copyProperties(userInfo,vo);
        vo.setFateValue(score.longValue());
        vo.setTags(StringUtils.split(userInfo.getTags(),","));
        // 返回
        return vo;
    }

    /**
     * 查看陌生人问题
     * @param userId
     * @return
     */
    public String queryStrangerQuestion(Long userId) {
        Question question = questionApi.findByUserId(userId);
        if(null != question){
            // 用户设置了陌生人问题
            return question.getTxt();
        }
        // 默认，如果用户没有设置陌生人问题。
        return "你真的喜欢我吗?";
    }

    public List<TodayBestVo> list() {
        Long userId = UserHolder.getUserId();
        PageResult<RecommendUser> pageResult = recommendUserApi.findPage(1,90,userId);
        // 查询用户信息
        List<RecommendUser> recommendUserList = pageResult.getItems();
        // 推荐用户的ids
        List<Long> userIds = recommendUserList.stream().map(RecommendUser::getUserId).collect(Collectors.toList());
        List<TodayBestVo> todayBestVoList = Collections.emptyList();
        if(userIds.size() > 0) {
            List<UserInfo> userInfoList = userInfoApi.findByIds(userIds);
            todayBestVoList = userInfoList.stream().map(userInfo -> {
                TodayBestVo bestVo = new TodayBestVo();
                BeanUtils.copyProperties(userInfo,bestVo,"tags");
                bestVo.setTags(userInfo.getTags().split(","));
                return bestVo;
            }).collect(Collectors.toList());
        }
        return todayBestVoList;
    }

    public void loveUser(Long userId) {
        userLikeApi.likeUser(UserHolder.getUserId(),userId);
    }

    public void unloveUser(Long userId) {
        userLikeApi.unLikeUser(UserHolder.getUserId(),userId);
    }


}
