package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/smallVideos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 发布小视频
     * @param videoThumbnail 封面图片
     * @param videoFile 视频
     * @return
     */
    @PostMapping
    public ResponseEntity post(MultipartFile videoThumbnail,MultipartFile videoFile){
        videoService.post(videoThumbnail, videoFile);
        return ResponseEntity.ok(null);
    }

    /**
     * 小视频列表
     * @return
     */
    @GetMapping
    public ResponseEntity videoList(@RequestParam(value = "page",defaultValue = "1") long page,
                                    @RequestParam(value = "pagesize",defaultValue = "10") long pagesize){
        page=page<1?1:page;// 防止page<1 导致分页功能报错
        // 调用业务分页查询
        PageResult pageResult = videoService.findPage(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 关注视频的作者
     * @param userId
     * @return
     */
    @PostMapping("/{userId}/userFocus")
    public ResponseEntity followUser(@PathVariable Long userId){
        videoService.followUser(userId);
        return ResponseEntity.ok(null);
    }

    /**
     * 取消关注视频的作者
     * @param uid
     * @return
     */
    @PostMapping("/{uid}/userUnFocus")
    public ResponseEntity unfollowUser(@PathVariable Long uid){
        videoService.unfollowUser(uid);
        return ResponseEntity.ok(null);
    }

}
