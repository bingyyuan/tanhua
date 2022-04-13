package com.tanhua.server.controller;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.CommentService;
import org.apache.yetus.audience.InterfaceAudience;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论controller
 */
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 动态的评论列表
     * @param page
     * @param pagesize
     * @param movementId 动态id
     * @return
     */
    @GetMapping
    public ResponseEntity findPage(@RequestParam(value = "page", defaultValue = "1") Long page,
                                   @RequestParam(value = "pagesize", defaultValue = "10") Long pagesize,
                                   String movementId){
        PageResult pageResult = commentService.findPage(page,pagesize,movementId);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 对动态发表评论
     * @return
     */
    @PostMapping
    public ResponseEntity add(@RequestBody Map<String,String> paramMap){
        // 添加评论，获取评论数
        int count = commentService.add(paramMap);
        return ResponseEntity.ok(count);
    }

    /**
     * 对评论点赞
     * 评论的id
     */
    @GetMapping("/{id}/like")
    public ResponseEntity like(@PathVariable String id){
        // 返回的点赞数
        Integer count = commentService.likeComment(id);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态 取消点赞
     * 动态的id
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable String id){
        // 返回的点赞数
        Integer count = commentService.dislikeComment(id);
        return ResponseEntity.ok(count);
    }
}
