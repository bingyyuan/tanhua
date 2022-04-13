package com.tanhua.server.controller;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.IMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 即时通讯的控制层
 */
@RestController
@RequestMapping("/messages")
public class IMController {

    @Autowired
    private IMService imService;

    /**
     * 添加联系人, 聊一聊的后续操作
     * @param param
     * @return
     */
    @PostMapping("/contacts")
    public ResponseEntity addContacts(@RequestBody Map<String,Integer> param){
        imService.addContacts(param.get("userId"));
        return ResponseEntity.ok(null);
    }

    /**
     * 联系人列表分页查询
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    @GetMapping("/contacts")
    public ResponseEntity queryContactsList(@RequestParam(value = "page",defaultValue = "1") Long page
                                            ,@RequestParam(value = "pagesize",defaultValue = "10") Long pagesize,
                                            //required = false可以不传这个参数
                                            @RequestParam(required = false) String keyword){
        PageResult pageResult = imService.queryContactsList(page,pagesize,keyword);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 谁点赞了我的列表信息
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/likes")
    public ResponseEntity likes(@RequestParam(value = "page",defaultValue = "1") Long page
            ,@RequestParam(value = "pagesize",defaultValue = "10") Long pagesize){
        // commentType=1 代表着点赞
        PageResult pageResult = imService.messageCommentList(1,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 谁评论了我的列表信息
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/comments")
    public ResponseEntity comments(@RequestParam(value = "page",defaultValue = "1") Long page
            ,@RequestParam(value = "pagesize",defaultValue = "10") Long pagesize){
        // commentType=2 代表着评论
        PageResult pageResult = imService.messageCommentList(2,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 谁喜欢了我的列表信息
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/loves")
    public ResponseEntity loves(@RequestParam(value = "page",defaultValue = "1") Long page
            ,@RequestParam(value = "pagesize",defaultValue = "10") Long pagesize){
        // commentType=3 代表着喜欢
        PageResult pageResult = imService.messageCommentList(3,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 公告列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/announcements")
    public ResponseEntity announcements(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult<Announcement> pageResult = imService.queryAnnouncements(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }
}
