package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.interfaces.PBEKey;
import java.util.Map;

/**
 * <p>
 * 通用设置
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/7
 */
@RestController
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * 用户通用设置 - 读取
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity getSettings(){
        // 查询登陆用户的设置信息
        SettingsVo vo = settingsService.getSettings();
        return ResponseEntity.ok(vo);
    }

    /**
     * 通知设置 - 保存
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity updateNotification(@RequestBody SettingsVo vo){
        // 调用业务更新
        settingsService.updateNotification(vo);
        return ResponseEntity.ok(null);
    }

    /**
     * 陌生人问题 - 保存
     */
    @PostMapping("/questions")
    public ResponseEntity updateQuestions(@RequestBody Map<String,String> param){
        // 调用业务更新
        settingsService.updateQuestions(param.get("content"));
        return ResponseEntity.ok(null);
    }

    /**
     * 黑名单分页查询
     */
    @GetMapping("/blacklist")
    public ResponseEntity blacklist(@RequestParam(value = "page",defaultValue = "1") long page,
                                    @RequestParam(value = "pagesize",defaultValue = "10") long pagesize){
        pagesize = pagesize>50?50:pagesize; // 防止pagesize过大
        // 分页查询
        PageResult<UserInfoVo> pageResult = settingsService.findBlackList(page,pagesize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 黑名单 移除
     */
    @DeleteMapping("blacklist/{uid}")
    public ResponseEntity delBlacklist(@PathVariable long uid){
        // 调用业务删除
        settingsService.delBlackList(uid);
        // 返回操作结果
        return ResponseEntity.ok(null);
    }

}
