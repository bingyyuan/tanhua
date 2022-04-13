package com.tanhua.server.controller;

import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.domain.db.User;
import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 用户信息的操作，个人中心。【我的】模块功能
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/6
 */
@RestController
@RequestMapping("/users")
public class UserInfoController {

    @Autowired
    private UserService userService;

    /**
     * 查询用户信息
     * @param userID
     * @param huanxinID
     * @param token
     * @return
     */
    @GetMapping
    public ResponseEntity getUserInfo(Long userID,Long huanxinID,@RequestHeader("Authorization") String token){
        // userID与huanxinID 暂时不处理先，等即时通讯时再来处理
        /*User user = userService.getUserByToken(token);
        if(null == user){
            throw new TanHuaException("登陆超时，请重新登陆");
        }*/
        // 通过用户id，查询用户详情
        UserInfoVo vo = userService.findUserInfoById(UserHolder.getUserId());
        return ResponseEntity.ok(vo);
    }

    /**
     * 【我的】，更新用户个人信息
     * 用户资料 - 保存
     * @param token
     * @param vo
     * @return
     */
    @PutMapping
    public ResponseEntity updateUserInfo(@RequestHeader("Authorization") String token, @RequestBody UserInfoVo vo){
        userService.updateUserInfo(token,vo);
        return ResponseEntity.ok(null);
    }

    /**
     * 【我的】更新头像
     * @param headPhoto
     * @param token
     * @return
     */
    @PostMapping("header")
    public ResponseEntity header(MultipartFile headPhoto, @RequestHeader("Authorization") String token){
        userService.header(headPhoto,token);
        return ResponseEntity.ok(null);
    }

    /**
     * 互相喜欢、喜欢、粉丝统计
     * @return
     */
    @GetMapping("/counts")
    public ResponseEntity counts(){
        CountsVo vo = userService.counts();
        return ResponseEntity.ok(vo);
    }

    /**
     * 互相喜欢、喜欢、粉丝、访客列表
     * @param type
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/friends/{type}")
    public ResponseEntity queryUserLikeList(@PathVariable int type,
                                            @RequestParam(defaultValue = "1") Long page,
                                            @RequestParam(defaultValue = "10") Long pagesize){
        page=page<1?1:page;
        PageResult pageResult = userService.queryUserLikeList(type,page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 粉丝中的喜欢
     * @param uid
     * @return
     */
    @PostMapping("fans/{uid}")
    public ResponseEntity fansLike(@PathVariable Long uid){
        userService.fansLike(uid);
        return ResponseEntity.ok(null);
    }

    /**
     * 喜欢 取消
     * @param uid
     * @return
     */
    @DeleteMapping("like/{uid}")
    public ResponseEntity unlike(@PathVariable Long uid){
        userService.unlike(uid);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{userId}/alreadyLove")
    public ResponseEntity alreadyLove(@PathVariable("userId") Long userId){
        boolean flag = userService.alreadyLove(userId);
        return ResponseEntity.ok(flag);
    }

}
