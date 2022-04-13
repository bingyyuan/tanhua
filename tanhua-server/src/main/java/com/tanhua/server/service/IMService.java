package com.tanhua.server.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.AnnouncementVo;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.*;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 即时通讯业务
 */
@Service
@Slf4j
public class IMService {

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private FriendApi friendApi;

    @Reference
    private CommentApi commentApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Reference
    private AnnouncementApi announcementApi;

    /**
     * 回复陌生人问题
     * @param param userId 佳人的id, reply 回复的内容
     */
    public void replyStrangerQuestion(Map<String, Object> param) {
        // 登陆用户的id
        Long loginUserId = UserHolder.getUserId();
        // 查询登陆用户信息
        UserInfo loginUserInfo = userInfoApi.findById(loginUserId);
        // 查询佳人的陌生人问题，如果没有问题，默认:"你真的喜欢我吗?
        Long userId = ((Integer) param.get("userId")).longValue();
        // 佳人的陌生人问题
        Question question = questionApi.findByUserId(userId);
        // 构建环信的消息结构
        Map<String, Object> map = new HashMap<>();
        map.put("userId", loginUserId.toString());
        map.put("nickname", loginUserInfo.getNickname());
        map.put("strangerQuestion", question==null?"你真的喜欢我吗?":question.getTxt());
        map.put("reply", ((String) param.get("reply")));
        String msg = JSON.toJSONString(map);
        log.info("回复陌生人问题消息内容:{}",msg);
        // 发送消息
        huanXinTemplate.sendMsg(userId.toString(), msg);
    }

    /**
     *  添加联系人, 聊一聊的后续操作
     * @param userId
     */
    public void addContacts(Integer userId) {
        // 添加为好友, 参数1：登陆用户, 参数2：对方的id
        friendApi.add(UserHolder.getUserId(), userId.longValue());
        // 调用环信，添加为好友。 【注意】：两个id必须在环信上有帐号
        huanXinTemplate.makeFriends(UserHolder.getUserId(), userId.longValue());
    }

    /**
     * 联系人列表分页查询
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    public PageResult queryContactsList(Long page, Long pagesize, String keyword) {
        // 获取登陆用户id
        Long userId = UserHolder.getUserId();
        // 调用friendApi查询好友分页数据
        PageResult pageResult = friendApi.findPage(userId, page,pagesize,keyword);
        // 获取分页的结果集
        List<Friend> friendList = pageResult.getItems();
        // 遍历结果集，获取每个好友的id
        if(CollectionUtils.isNotEmpty(friendList)){
            // 转成vo List<Vo>
            List<ContactVo> voList = new ArrayList<ContactVo>();
            for (Friend friend : friendList) {
                ContactVo vo = new ContactVo();
                // 查询好友信息
                UserInfo friendUserInfo = userInfoApi.findById(friend.getFriendId());
                BeanUtils.copyProperties(friendUserInfo, vo);
                // 前端需要的是字符串，好友的id
                vo.setUserId(friendUserInfo.getId().toString());
                voList.add(vo);
            }
            // 设置到分页结果集
            pageResult.setItems(voList);
            // 返回pageResult
        }
        return pageResult;
    }

    /**
     * 谁评论、点赞、喜欢了我
     * @param commentType 1：点赞，2：评论，3：喜欢
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult messageCommentList(int commentType, Long page, Long pagesize) {
        // 分页查询评论数据
        PageResult pageResult = commentApi.findByUserId(page,pagesize,commentType,UserHolder.getUserId());
        List<Comment> commentList = pageResult.getItems();
        List<MessageVo> voList = new ArrayList<MessageVo>();
        if(CollectionUtils.isNotEmpty(commentList)) {
            // 遍历结果集
            for (Comment comment : commentList) {
                // 查询评论人的信息
                UserInfo userInfo = userInfoApi.findById(comment.getUserId());
                // 转成vo
                MessageVo vo = new MessageVo();
                BeanUtils.copyProperties(userInfo,vo);
                // 设置评论的id
                vo.setId(comment.getId().toHexString());
                // 设置评论的时间
                vo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));
                voList.add(vo);
            }
            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 公告列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult queryAnnouncements(Integer page, Integer pagesize) {
        //1、调用API查询分页数据 PageResult
        PageResult pageResult = announcementApi.findPage(page, pagesize);
        //2、获取所有的公告对象
        List<Announcement> records = pageResult.getItems();
        //3、一个公告对象，转化为一个vo对象
        List<AnnouncementVo> list = new ArrayList<>();

        for (Announcement record : records) {
            AnnouncementVo vo = new AnnouncementVo();
            BeanUtils.copyProperties(record,vo);
            if(record.getCreated() != null) {
                vo.setCreateDate(DateUtil.date(record.getCreated()).toDateStr());
            }
            list.add(vo);
        }
        pageResult.setItems(list);
        return pageResult;
    }
}
