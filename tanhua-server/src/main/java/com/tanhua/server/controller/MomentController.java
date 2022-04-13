package com.tanhua.server.controller;

import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.server.service.CommentService;
import com.tanhua.server.service.MomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 动态相关操作类
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/10
 */
@RestController
@RequestMapping("/movements")
public class MomentController {

    @Autowired
    private MomentService momentService;

    @Autowired
    private CommentService commentService;

    /**
     * 发布动态
     */
    @PostMapping
    public ResponseEntity postMoment(PublishVo vo, MultipartFile[] imageContent){
        // 调用service发布
        momentService.postMoment(vo, imageContent);
        // 返回结果
        return ResponseEntity.ok(null);
    }

    /**
     * 查询好友动态
     * @return
     */
    @GetMapping
    public ResponseEntity queryFriendPublishList(@RequestParam(value = "page",defaultValue = "1") long page,
                                                 @RequestParam(value = "pagesize",defaultValue = "10") long pagesize){
        PageResult<MomentVo> pageResult = momentService.queryFriendPublishList(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 推荐动态
     */
    @GetMapping("/recommend")
    public ResponseEntity queryRecommendPublishList(@RequestParam(value = "page",defaultValue = "1") long page,
                                                 @RequestParam(value = "pagesize",defaultValue = "10") long pagesize){
        page=page<1?1:page;
        PageResult<MomentVo> pageResult = momentService.queryRecommendPublishList(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 我的动态
     */
    @GetMapping("/all")
    public ResponseEntity queryMyAlbum(@RequestParam(value = "page",defaultValue = "1") long page,
                                       @RequestParam(value = "pagesize",defaultValue = "10") long pagesize,
                                       Long userId){
        PageResult<MomentVo> pageResult = momentService.queryMyAlbum(page,pagesize,userId);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 动态点赞
     * 动态的id
     */
    @GetMapping("/{id}/like")
    public ResponseEntity like(@PathVariable String id){
        // 返回的点赞数
        Integer count = commentService.like(id);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态 取消点赞
     * 动态的id
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable String id){
        // 返回的点赞数
        Integer count = commentService.dislike(id);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态喜欢
     * 动态的id
     */
    @GetMapping("/{id}/love")
    public ResponseEntity love(@PathVariable String id){
        // 返回的喜欢数
        Integer count = commentService.love(id);
        return ResponseEntity.ok(count);
    }

    /**
     * 动态 取消喜欢
     * 动态的id
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity unlove(@PathVariable String id){
        // 返回的喜欢数
        Integer count = commentService.unlove(id);
        return ResponseEntity.ok(count);
    }

    /**
     * 单条动态
     * 动态的id /visitors
     */
    @GetMapping("/{id}")
    public ResponseEntity queryById(@PathVariable String id){
        // 返回的动态信息
        if("visitors".equals(id)){
            return ResponseEntity.ok(null);
        }
        MomentVo vo = momentService.findById(id);
        return ResponseEntity.ok(vo);
    }

    /**
     * 谁看过我
     */
    @GetMapping("/visitors")
    public ResponseEntity queryVisitors(){
        List<VisitorVo> voList = momentService.queryVisitors();
        return ResponseEntity.ok(voList);
    }




}
