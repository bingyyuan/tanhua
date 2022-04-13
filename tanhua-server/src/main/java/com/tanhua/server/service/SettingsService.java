package com.tanhua.server.service;

import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.Settings;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 通用设置的业务类
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/7
 */
@Service
@Slf4j
public class SettingsService {

    @Reference
    private QuestionApi questionApi;

    @Reference
    private SettingsApi settingsApi;

    @Reference
    private BlackListApi blackListApi;


    /**
     * 查询登陆用户的设置信息
     * @return
     */
    public SettingsVo getSettings() {
        //1. 获取登陆用户信息
        User user = UserHolder.getUser();
        //2. 调用陌生人问题的api
        Question question = questionApi.findByUserId(user.getId());
        //3. 调用通知设置的api
        Settings settings = settingsApi.findByUserId(user.getId());
        //4. 构建vo
        SettingsVo vo = new SettingsVo();
        //5. 设置vo里的值
        if(null != settings) {
            BeanUtils.copyProperties(settings, vo);
        }
        // 设置陌生人问题
        if(null != question) {
            vo.setStrangerQuestion(question.getTxt());
        }
        // 设置手机号码
        vo.setPhone(user.getMobile());
        //6. 返回
        return vo;
    }

    /**
     * 更新通知设置
     * @param vo
     */
    public void updateNotification(SettingsVo vo) {
        // 1. 获取登陆用户id
        Long userId = UserHolder.getUserId();
        // 通过用户id查询通知设置信息
        Settings settings = settingsApi.findByUserId(userId);
        log.info("数据库中的settings=========={}",settings);
        // 存在
        if(null != settings){
            //复制属性的值,vo的id是没有值，排除id不复制
            BeanUtils.copyProperties(vo,settings,"id");
            // 存在则更新
            settingsApi.update(settings);
        }else{
            //构建pojo Settings
            settings = new Settings();
            //复制属性的值
            BeanUtils.copyProperties(vo,settings);
            // 设置用户的id
            settings.setUserId(userId);
            // 不存在 添加
            settingsApi.save(settings);
        }
    }

    /**
     * 陌生人问题 - 保存
     * @param content
     */
    public void updateQuestions(String content) {
        // 1.获取登陆用户的id
        Long userId = UserHolder.getUserId();
        // 2.通过用户id查询陌生人问题
        Question question = questionApi.findByUserId(userId);
        // 3.有数据
        if(null != question) {
            //   更新,设置新的问题，调用api更新
            question.setTxt(content);
            questionApi.update(question);
        }else {
            // 4.没数据
            question = new Question();
            //   设置用户id,再添加
            question.setTxt(content);
            question.setUserId(userId);
            questionApi.save(question);
        }
    }

    /**
     * 黑名单分页查询
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<UserInfoVo> findBlackList(long page, long pagesize) {
        // 获取用户id
        Long userId = UserHolder.getUserId();
        // 调用api分页查询
        PageResult pageResult = blackListApi.findBlackList(page,pagesize,userId);
        // 把UserInfo转成vo, 可以转也可不转成vo，如果是要按规范（企业）走
        // 分页的结果集
        List<UserInfo> items = (List<UserInfo>)pageResult.getItems();
        /*if(!CollectionUtils.isNotEmpty(items)){
            // 接收的集合，转在的目标集合
            List<UserInfoVo> list = new ArrayList<UserInfoVo>();
            // 遍历查询到的结果集
            for (UserInfo item : items) {
                // 创建 vo对象
                UserInfoVo vo = new UserInfoVo();
                // 复制属性值
                BeanUtils.copyProperties(item, vo);
                vo.setAge(item.getAge().toString());
                // 添加到新集合里
                list.add(vo);
            }
            *//*List<UserInfoVo> list = items.stream().map(userInfo -> {
                UserInfoVo vo = new UserInfoVo();
                BeanUtils.copyProperties(userInfo, vo);
                return vo;
            }).collect(Collectors.toList());*//*
            pageResult.setItems(list);
        }*/
        // 再返回给
        return pageResult;
    }

    /**
     * 移除黑名单
     * @param uid 黑名单的id black_user_id
     */
    public void delBlackList(long uid) {
        // 获取登陆用户id
        Long userId = UserHolder.getUserId();
        log.info("移除黑名单: userId={}, blackUserId={}",userId,uid);
        // 调用api移除
        blackListApi.delete(userId, uid);
    }
}
