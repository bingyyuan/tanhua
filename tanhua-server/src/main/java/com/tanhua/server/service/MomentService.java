package com.tanhua.server.service;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.dubbo.api.PublishApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VisitorsApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.io.PipedReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  动态业务类
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/10
 */
@Service
@Slf4j
public class MomentService {

    @Reference
    private PublishApi publishApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Reference
    private VisitorsApi visitorsApi;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发布动态
     * @param vo
     * @param imageContent
     */
    public void postMoment(PublishVo vo, MultipartFile[] imageContent) {
        log.info("开发发布动态.............");
        //1. 获取登陆用户id
        Long userId = UserHolder.getUserId();
        log.info("登陆用户的id={}", userId);
        //2. 循环遍历上传图片,List图片的url
        List<String> imageUrls = new ArrayList<String>();
        if(null != imageContent){
            try {
                for (MultipartFile file : imageContent) {
                    String url = ossTemplate.upload(file.getOriginalFilename(), file.getInputStream());
                    log.info("上传的图片：{}",url);
                    imageUrls.add(url);
                }
            }catch (IOException ex){
                throw new TanHuaException("上传图片失败");
            }
        }
        //3. 构建publish对象
        Publish publish = new Publish();
        BeanUtils.copyProperties(vo,publish);
        publish.setUserId(userId);
        publish.setMedias(imageUrls);
        ObjectId publishId = ObjectId.get();
        publish.setId(publishId);
        log.info("动态的详情: {}", publish);
        // 调用服务发布动态
        publishApi.add(publish);
        // 发送动态审核的消息动态的id
        rocketMQTemplate.convertAndSend("tanhua-publish",publishId.toHexString());
        log.info("发布动态成功...........");
    }

    /**
     * 查询好友动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MomentVo> queryFriendPublishList(long page, long pagesize) {
        // 获取登陆用户id
        Long userId = UserHolder.getUserId();
        // 通过用户id分页查询好友动态
        PageResult pageResult = publishApi.findFriendPublishByTimeline(page,pagesize,userId);
        // 获取查询的结果集(动态的结果)
        List<Publish> publishList = pageResult.getItems();
        // 遍历结果集，查询用户详情
        //构建List vo
        List<MomentVo> voList = new ArrayList<MomentVo>();
        if(null != publishList){
            for (Publish publish : publishList) {
                Long publisherId = publish.getUserId(); // 发布者的id, 好友的id
                // 查询好友的详情
                UserInfo userInfo = userInfoApi.findById(publisherId);
                MomentVo vo = new MomentVo();
                // 复制用户详情
                BeanUtils.copyProperties(userInfo,vo);
                vo.setTags(StringUtils.split(userInfo.getTags(),","));
                // 复制动态的信息
                BeanUtils.copyProperties(publish, vo);
                vo.setImageContent(publish.getMedias().toArray(new String[]{}));
                // 设置动态的id
                vo.setId(publish.getId().toHexString());
                // 默认距离
                vo.setDistance("100米");
                String key = "publish_like_" + userId+"_" + vo.getId();
                vo.setHasLiked(0);  //是否点赞  0：未点 1:点赞
                if(redisTemplate.hasKey(key)) {
                    vo.setHasLiked(1);  //用户对这个动态点赞了
                }
                String keyLove = "publish_love_" + userId+"_" + vo.getId();
                vo.setHasLoved(0);  //是否喜欢  0：未点 1:点赞
                if(redisTemplate.hasKey(keyLove)) {
                    vo.setHasLoved(1);  //用户对这个动态喜欢了
                }
                // 设置时间
                vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
                // 添加到list中
                voList.add(vo);
            }
        }
        //设置pageResult再返回
        pageResult.setItems(voList);
        return pageResult;
    }

    /**
     * 推荐动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult<MomentVo> queryRecommendPublishList(long page, long pagesize) {
        // 获取登陆用户id
        Long userId = UserHolder.getUserId();
        // 通过用户id分页查询推荐动态
        PageResult pageResult = publishApi.findRecommendPublish(page,pagesize,userId);
        // 获取查询的结果集(动态的结果)
        List<Publish> publishList = pageResult.getItems();
        // 遍历结果集，查询用户详情
        //构建List vo
        List<MomentVo> voList = new ArrayList<MomentVo>();
        if(null != publishList){
            for (Publish publish : publishList) {
                Long publisherId = publish.getUserId(); // 发布者的id, 好友的id
                // 查询推荐用户的详情
                UserInfo userInfo = userInfoApi.findById(publisherId);
                MomentVo vo = new MomentVo();
                // 复制用户详情
                BeanUtils.copyProperties(userInfo,vo);
                vo.setTags(StringUtils.split(userInfo.getTags(),","));
                // 复制动态的信息
                BeanUtils.copyProperties(publish, vo);
                vo.setImageContent(publish.getMedias().toArray(new String[]{}));
                // 设置动态的id
                vo.setId(publish.getId().toHexString());
                // 默认距离
                vo.setDistance("100米");
                String key = "publish_like_" + userId+"_" + vo.getId();
                vo.setHasLiked(0);  //是否点赞  0：未点 1:点赞
                if(redisTemplate.hasKey(key)) {
                    vo.setHasLiked(1);  //用户对这个动态点赞了
                }
                String keyLove = "publish_love_" + userId+"_" + vo.getId();
                vo.setHasLoved(0);  //是否喜欢  0：未点 1:点赞
                if(redisTemplate.hasKey(keyLove)) {
                    vo.setHasLoved(1);  //用户对这个动态喜欢了
                }
                // 设置时间
                vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
                // 添加到list中
                voList.add(vo);
            }
        }
        //设置pageResult再返回
        pageResult.setItems(voList);
        return pageResult;
    }

    /**
     * 我的动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    public PageResult<MomentVo> queryMyAlbum(long page, long pagesize, Long userId) {
        // 获取登陆用户的id
        if(null == userId){
            userId = UserHolder.getUserId();
        }
        // 调用mongo查询
        PageResult pageResult = publishApi.findMyAlbum(page, pagesize,userId);
        // 补全用户信息
        UserInfo userInfo = userInfoApi.findById(userId);
        List<Publish> publishList = pageResult.getItems();
        // 转成vo
        List<MomentVo> list = new ArrayList<MomentVo>();
        MomentVo vo = null;
        for (Publish publish : publishList) {
            vo = new MomentVo();
            // 复制用户详情
            BeanUtils.copyProperties(userInfo,vo);
            vo.setTags(StringUtils.split(userInfo.getTags(),","));
            // 复制动态的信息
            BeanUtils.copyProperties(publish, vo);
            vo.setImageContent(publish.getMedias().toArray(new String[]{}));
            // 设置动态的id
            vo.setId(publish.getId().toHexString());
            // 默认距离
            vo.setDistance("100米");
            String key = "publish_like_" + userId+"_" + vo.getId();
            vo.setHasLiked(0);  //是否点赞  0：未点 1:点赞
            if(redisTemplate.hasKey(key)) {
                vo.setHasLiked(1);  //用户对这个动态点赞了
            }
            String keyLove = "publish_love_" + userId+"_" + vo.getId();
            vo.setHasLoved(0);  //是否喜欢  0：未点 1:点赞
            if(redisTemplate.hasKey(keyLove)) {
                vo.setHasLoved(1);  //用户对这个动态喜欢了
            }
            // 设置时间
            vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
            list.add(vo);
        }
        pageResult.setItems(list);
        return pageResult;
    }

    /**
     * 单条动态
     * @param publishId
     * @return
     */
    public MomentVo findById(String publishId) {
        // 调用publishApi查询动态信息
        Publish publish = publishApi.findById(publishId);
        // 获取动态的发布者id
        Long userId = publish.getUserId();
        // 调用userInfoApi查询用户信息
        UserInfo userInfo = userInfoApi.findById(userId);
        // 构建vo
        MomentVo vo = new MomentVo();
        // 补全vo信息
        // 复制用户详情
        BeanUtils.copyProperties(userInfo,vo);
        vo.setTags(StringUtils.split(userInfo.getTags(),","));
        // 复制动态的信息
        BeanUtils.copyProperties(publish, vo);
        vo.setImageContent(publish.getMedias().toArray(new String[]{}));
        // 设置动态的id
        vo.setId(publish.getId().toHexString());
        // 默认距离
        vo.setDistance("100米");
        String key = "publish_like_" + UserHolder.getUserId() + "_" + vo.getId();
        vo.setHasLiked(0);  //是否点赞  0：未点 1:点赞
        if(redisTemplate.hasKey(key)) {
            vo.setHasLiked(1);  //用户对这个动态点赞了
        }
        String keyLove = "publish_love_" + UserHolder.getUserId() + "_" + vo.getId();
        vo.setHasLoved(0);  //是否喜欢  0：未点 1:点赞
        if(redisTemplate.hasKey(keyLove)) {
            vo.setHasLoved(1);  //用户对这个动态喜欢了
        }
        // 设置时间
        vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));
        // 返回vo
        return vo;
    }

    /**
     * 谁看过我
     * @return
     */
    public List<VisitorVo> queryVisitors() {
        Long userId = UserHolder.getUserId();
        //1. redis是否记录上一次登陆时间
        String key = "visitors_time_" + userId;
        String lastTime = (String)redisTemplate.opsForValue().get(key);
        //2. 有值：查询访客记录时，要带上这个时间条件
        //3. 没值：查询访客记录时，不需要时间条件
        List<Visitor> visitorList = visitorsApi.queryVisitors(userId, lastTime);
        //4. 查询访客的用户信息，
        List<VisitorVo> voList = new ArrayList<VisitorVo>();
        if(CollectionUtils.isNotEmpty(visitorList)){
            for (Visitor visitor : visitorList) {
                // 访客的id
                Long visitorUserId = visitor.getVisitorUserId();
                // 查询访客信息
                UserInfo visitorUserInfo = userInfoApi.findById(visitorUserId);
                VisitorVo vo = new VisitorVo();
                BeanUtils.copyProperties(visitorUserInfo, vo);
                vo.setTags(StringUtils.split(visitorUserInfo.getTags(),","));
                // 缘分值
                vo.setFateValue(visitor.getScore().intValue());
                voList.add(vo);
            }
        }
        //5. 查询完后，使用redis记录这一次的查询的时间
        redisTemplate.opsForValue().set(key,System.currentTimeMillis()+"");
        //6. 转成vo再返回
        return voList;
    }
}
