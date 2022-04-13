package com.tanhua.server.controller;

import com.tanhua.commons.vo.HuanXinUser;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/huanxin")
@Slf4j
public class HuanXinController {

    /**
     * 获取当前登陆的用户名与密码，用于环信的登陆
     * @return
     */
    @GetMapping("/user")
    public ResponseEntity<HuanXinUser> getLoginHuanXinUser(){
        // 返回 登陆用户的信息. 用户的id必须与环信上的用户id一致。
        // 密码: 123456。HuanXinTemplate注册方法里写死密码为123456
        HuanXinUser user = new HuanXinUser(UserHolder.getUserId().toString(), "123456",String.format("今晚打老虎_%d",100));
        log.info("获取环信用户信息成功========={}",user);
        return ResponseEntity.ok(user);
    }
}