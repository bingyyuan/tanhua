package com.tanhua.manage.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.interceptor.AdminHolder;
import com.tanhua.manage.mapper.AdminMapper;
import com.tanhua.manage.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    private static final String CACHE_KEY_CAP_PREFIX = "MANAGE_CAP_";
    public static final String CACHE_KEY_TOKEN_PREFIX="MANAGE_TOKEN_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 保存生成的验证码
     * @param uuid
     * @param code
     */
    public void saveCode(String uuid, String code) {
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        // 缓存验证码，10分钟后失效
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(10));
    }

    /**
     * 获取登陆用户信息
     * @return
     */
    public Admin getByToken(String authorization) {
        String token = authorization.replaceFirst("Bearer ","");
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        // 登录成功后放入的
        String adminString = (String) redisTemplate.opsForValue().get(tokenKey);
        Admin admin = null;
        if(StringUtils.isNotEmpty(adminString)) {
            admin = JSON.parseObject(adminString, Admin.class);
            // 延长有效期 30分钟
            redisTemplate.expire(tokenKey,30, TimeUnit.MINUTES);
        }
        return admin;
    }

    /**
     * 登陆校验
     * @param paramMap
     * @return
     */
    public Map<String, String> login(Map<String, String> paramMap) {
        // 1. 获取参数
        String username = paramMap.get("username");
        String password = paramMap.get("password");
        String verificationCode = paramMap.get("verificationCode");
        String uuid = paramMap.get("uuid");
        // 2. 用户名与密码的非空判断
        if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
            throw new BusinessException("用户名或密码不能为空!");
        }
        // 3. 校验验证码
        //   3.1 取redis中的验证码, 拼接key
        String validateCodeKey = CACHE_KEY_CAP_PREFIX + uuid;
        // 取出验证码
        String codeInRedis = (String) redisTemplate.opsForValue().get(validateCodeKey);
        // 判断字符串是否相等，不会出现空指针
        if(!StringUtils.equals(verificationCode, codeInRedis)){
            throw new BusinessException("验证不正确，请重新获取!");
        }
        //   3.2 校验通过要删除Key
        redisTemplate.delete(validateCodeKey);
        // 4. 校验用户名与密码是否正确,数据的密码是加密的md5
        // 调用AdminMapper接口的泛型指定好了，构建查询条件，QueryWrapper(where 列名=值)
        // eq =
        // select * from tb_admin where username=? and password=?
        Admin admin = query().eq("username", username)
                .eq("password", SecureUtil.md5(password)).one();
        if(null == admin){
            throw new BusinessException("用户名或密码错误");
        }
        // 5. 校验通过,生成token
        String token = jwtUtils.createJWT(admin.getUsername(), admin.getId());
        // 6. 把用户信息存入redis
        String adminJson = JSON.toJSONString(admin);
        redisTemplate.opsForValue().set(CACHE_KEY_TOKEN_PREFIX+token,adminJson);
        // 7. 返回token map
        Map<String,String> result = new HashMap<String,String>();
        result.put("token",token);
        return result;
    }

    /**
     * 退出登陆
     * @param token
     */
    public void logout(String token) {
        token = token.replace("Bearer ", "");
        String key = CACHE_KEY_TOKEN_PREFIX + token;
        // 删除redis中的key
        redisTemplate.delete(key);
        // 设置AdminHolder中的值为null
        AdminHolder.set(null);
    }
}
