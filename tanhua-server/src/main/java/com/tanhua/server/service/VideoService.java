package com.tanhua.server.service;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 小视频业务
 */
@Service
@Slf4j
public class VideoService {

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient client;
    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Reference
    private VideoApi videoApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布小视频
     * @param videoThumbnail
     * @param videoFile
     */
    @CacheEvict(value = "videoList",allEntries = true)
    public void post(MultipartFile videoThumbnail, MultipartFile videoFile)  {
        log.info("===========移除缓存...........");
        try {
            // 封面图片上传给oss
            String picUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
            // 上传视频到fastDfs
            String videoName = videoFile.getOriginalFilename();
            // 视频文件的后缀扣
            String suffix = videoName.substring(videoName.lastIndexOf(".") + 1);
            StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), suffix, null);
            // 视频的完整路径
            String videoUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
            // 构建video对象
            Video video = new Video();
            video.setVideoUrl(videoUrl);
            video.setPicUrl(picUrl);
            video.setCreated(System.currentTimeMillis());
            video.setUserId(UserHolder.getUserId());
            // 调用api保存video
            videoApi.save(video);
        } catch (IOException e) {
            log.error("上传封面图片失败",e);
            throw new TanHuaException("上传封面图片失败");
        }

    }

    /**
     * 小视频列表videoList::1_10
     * @param page
     * @param pagesize
     * @return
     */
    @Cacheable(value = "videoList", key = "#page + '_' + #pagesize")
    public PageResult findPage(long page, long pagesize) {
        log.info("===========调用api查询视频信息...........");
        // 调用api查询分页视频列表
        PageResult pageResult = videoApi.findPage(page,pagesize);
        // 获取结果集
        List<Video> videoList = pageResult.getItems();
        // 遍历它，转成voList
        List<VideoVo> voList = new ArrayList<VideoVo>();
        if(CollectionUtils.isNotEmpty(videoList)){
            for (Video video : videoList) {
                VideoVo vo = new VideoVo();
                // 查询用户的信息
                UserInfo userInfo = userInfoApi.findById(video.getUserId());
                BeanUtils.copyProperties(userInfo,vo);
                BeanUtils.copyProperties(video,vo);
                // 视频的id, 16进制的字符串
                vo.setId(video.getId().toHexString());
                // 封面图片
                vo.setCover(video.getPicUrl());
                // 签名，水印
                vo.setSignature("黑马程序员");
                // 是否点赞 后续修改
                // 是否关注 后续修改
                String key = "follow_user_" + UserHolder.getUserId() + "_" + video.getUserId();
                if (redisTemplate.hasKey(key)) {
                    vo.setHasFocus(1); // 关注了
                }

                // 复制属性的值
                voList.add(vo);
            }
            pageResult.setItems(voList);
        }
        return pageResult;
    }

    /**
     * 关注视频的作者
     * @param userId
     */
    public void followUser(Long userId) {
        // 调用api保存关系
        FollowUser followUser = new FollowUser();
        followUser.setUserId(UserHolder.getUserId()); // 登陆用户来关注他人
        followUser.setFollowUserId(userId); // 视频的作者id
        followUser.setCreated(System.currentTimeMillis());
        videoApi.followUser(followUser);
        // 存入redis，标记登陆用户关注了这个作者
        String key = "follow_user_" + UserHolder.getUserId() + "_" + userId;
        redisTemplate.opsForValue().set(key,1);
    }

    /**
     * 取消关注
     * @param userId
     */
    public void unfollowUser(Long userId) {
        FollowUser followUser = new FollowUser();
        followUser.setUserId(UserHolder.getUserId()); // 登陆用户来关注他人
        followUser.setFollowUserId(userId); // 视频的作者id
        // 删除关注
        videoApi.unfollowUser(followUser);
        // 删除redis中的关注标记
        String key = "follow_user_" + UserHolder.getUserId() + "_" + userId;
        redisTemplate.delete(key);
    }
}
