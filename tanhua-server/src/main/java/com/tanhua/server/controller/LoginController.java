package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/4
 */
@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;

    /*@GetMapping("/findUser")
    public ResponseEntity<User> findUser(String mobile){
        // 通过手机号码查询
        User user = userService.findByMobile(mobile);
        // 响应
        return ResponseEntity.ok(user);
    }

    @PostMapping("/saveUser")
    public ResponseEntity saveUser(@RequestBody Map<String,String> param){
        String mobile = param.get("mobile");
        String password = param.get("password");
        // 保存
        userService.saveUser(mobile,password);
        return ResponseEntity.ok("保存成功");
    }*/

    /**
     * 登陆时发送验证码
     * @param param
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity sendValidateCode(@RequestBody Map<String,String> param){
        // 获取手机号码
        String phone = param.get("phone");
        // 调用业务service发送
        userService.sendValidateCode(phone);
        // 响应结果
        return ResponseEntity.ok(null);
    }

    /**
     * 登录第二步---验证码校验
     * @return
     */
    @PostMapping("loginVerification")
    public ResponseEntity loginVerification(@RequestBody Map<String,String> param){
        // 获取手机 号码
        String phone = param.get("phone");
        // 获取过来的验证码
        String verificationCode = param.get("verificationCode");
        // 验证登陆
        Map<String,Object> result = userService.loginVerification(phone,verificationCode);
        // 结果返回给apk
        return ResponseEntity.ok(result);
    }

    /**
     * 户---1填写资料
     * @param token
     * @param userInfoVo
     * @return
     */
    @PostMapping("loginReginfo")
    public ResponseEntity loginReginfo(@RequestHeader("Authorization") String token,
                                       @RequestBody UserInfoVo userInfoVo){
        // 转成userInfo
        UserInfo userInfo = new UserInfo();
        // copyProperties 复制属性值(属性性名一样就会复制，不一样就忽略)
        // 第一个参数，来源，userInfoVo 复制到 userInfo, 来源就是userInfoVo
        // 第二个参数，目标对象。userInfo
        BeanUtils.copyProperties(userInfoVo,userInfo);
        // 调用UserService 完成注册，
        userService.saveUserInfo(userInfo,token);
        return ResponseEntity.ok(null);
    }

    /**
     * 上传用户头像
     * 新用户---2选取头像
     */
    @PostMapping("loginReginfo/head")
    public ResponseEntity uploadAvatar(MultipartFile headPhoto, @RequestHeader("Authorization") String token){
        userService.uploadAvatar(headPhoto,token);
        return ResponseEntity.ok(null);
    }
}
