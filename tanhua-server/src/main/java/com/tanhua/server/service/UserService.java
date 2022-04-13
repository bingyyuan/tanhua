package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.sun.media.jfxmedia.logging.Logger;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.FriendApi;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.UserLikeApi;
import com.tanhua.server.TanhuaServerApplication;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.GetAgeUtil;
import com.tanhua.server.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/4
 */
@Service // 【注意】：这是消费者，不能使用dubbo的。
@Slf4j
public class UserService {

    @Reference
    private UserApi userApi;

    @Reference
    private UserLikeApi userLikeApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送验证码工具类
     */
    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FaceTemplate faceTemplate;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * redis业务前缀标识
     */
    @Value("${tanhua.redisValidateCodeKeyPrefix}")
    private String redisValidateCodeKeyPrefix;

    //public User findByMobile(String mobile) {
    //    return userApi.findByMobile(mobile);
    //}
//
    //public void saveUser(String mobile, String password) {
    //    User user = new User();
    //    user.setMobile(mobile);
    //    user.setPassword(password);
    //    user.setCreated(new Date());
    //    userApi.save(user);
    //}

    /**
     * 发送验证码
     * @param phone
     */
    public void sendValidateCode(String phone) {
        //1. 构建redis中验证的key
        String key = redisValidateCodeKeyPrefix + phone;
        //2. 获取redis中验证码
        String codeInRedis = (String)redisTemplate.opsForValue().get(key);
        log.info("codeInRedis:{}",codeInRedis);

        if(StringUtils.isNotEmpty(codeInRedis)) {
            //3. 有值
            //  3.1 返回验证码未失效，抛出自定义异常
            throw new TanHuaException(ErrorResult.duplicate());
        }else {
            //4. 没有
            //5. 生成验证码
            String validateCode = "123456";//RandomStringUtils.randomNumeric(6);
            log.info("=============验证码:{},{}",phone, validateCode);
            //6. 发送验证码
            //Map<String, String> smsRs = smsTemplate.sendValidateCode(phone,validateCode);
            //if(null != smsRs){
            //    // 验证码发送失败，报错
            //    throw new TanHuaException(ErrorResult.fail());
            //}
            //7. 存入redis
            redisTemplate.opsForValue().set(key,validateCode, Duration.ofMinutes(10));
        }
    }

    /**
     * 登陆验证
     * @param phone
     * @param verificationCode
     * @return
     */
    public Map<String, Object> loginVerification(String phone, String verificationCode) {
        log.info("进入登陆验证码的校验.......");
        //1.验证验证码 (删除redis中的验证码)
        // 构建key
        String key = redisValidateCodeKeyPrefix + phone;
        // 获取redis中的验证码
        String codeInRedis = (String)redisTemplate.opsForValue().get(key);
        log.info("验证码信息: {},{},{}.......",phone, verificationCode, codeInRedis);
        // 验证码的校验
        //2.不通过，报错
        //3.通过
        if(StringUtils.isEmpty(codeInRedis)){
            // 验证码过时了
            throw new TanHuaException(ErrorResult.loginError());
        }
        if(!codeInRedis.equals(verificationCode)){
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        // 防止重复提交
        redisTemplate.delete(key);

        //4.通过手机号码查询用户是否存在
        User user = userApi.findByMobile(phone);
        log.info("用户信息: {}", user==null?"不存在":"存在");
        //5.不存在：创建用户(调用userApi)
        //6.存在
        //isNew:false
        boolean isNew = false;
        //消息类型为：用户登陆
        String type = "0101";
        if(null == user){
            user = new User();
            user.setCreated(new Date());
            user.setUpdated(new Date());
            user.setPassword("123456");
            user.setMobile(phone);
            // 保存
            Long userId = userApi.save(user);
            //isNew:true
            isNew = true; // 新创建的
            user.setId(userId);
            // 注册环信, 默认密码为123456
            huanXinTemplate.register(userId);
            //消息类型为：用户注册
            type="0102";
        }
        //7.创建token
        String token = jwtUtils.createJWT(phone, user.getId());
        log.info("创建的token: {}", token);
        // 把用户信息转成json格式的字符串
        String userString = JSON.toJSONString(user);
        //8.存入redis(当用户再次请求后台时，校验token有效性),
        // 存入的值是用户信息，后期通过redis直接可获取登陆用户的信息
        redisTemplate.opsForValue().set("TOKEN_" + token, userString,Duration.ofDays(7));
        //9.构建返回对象(token,isNew)
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("token",token);
        result.put("isNew",isNew);
        //10.返回给controller

        // 写消息
        Map<String,Object> message = new HashMap<>();
        // 登陆用户
        message.put("userId",user.getId());
        message.put("type",type);
        message.put("date",new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        // 发消息
        rocketMQTemplate.convertAndSend("tanhua_log",message);
        return result;
    }

    /**
     * 通过token获取登陆用户信息
     *
     * key: 一般 带前缀 _  ::
     * @param token
     * @return
     */
    public User getUserByToken(String token){
        String key = "TOKEN_" + token;
        String userString = (String) redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(userString)){
            // 用户超时了，返回null
            return null;
        }
        // 解析json字符串为登陆用户
        User user = JSON.parseObject(userString, User.class);
        // 重置有效期 expire, Durations.ofDays(7), 有效期7天
        redisTemplate.expire(key,7, TimeUnit.DAYS);
        return user;
    }

    /**
     * 保存用户信息
     * @param userInfo
     * @param token
     */
    public void saveUserInfo(UserInfo userInfo,String token) {
        //1. 从token中来获取登陆用户信息, 保存 tb_userInfo.id=登陆用户的id
        /*User user = getUserByToken(token);
        if(null == user){
            throw new TanHuaException("登陆超时，请重新登陆");
        }*/

        //2. 设置用户的id
        userInfo.setId(UserHolder.getUserId());
        // 通过出生日期来获取年龄
        userInfo.setAge(GetAgeUtil.getAge(userInfo.getBirthday()));
        //3. 调用服务保存用户信息
        userInfoApi.save(userInfo);
    }

    /**
     * 新用户---2选取头像
     * @param headPhoto
     * @param token
     */
    public void uploadAvatar(MultipartFile headPhoto, String token) {
        log.info("开始上传用户头像处理................");
        // 获取登陆用户信息
        /*User user = getUserByToken(token);
        // 用户失效判断
        if(null == user) {
            // 失效则报错
            throw new TanHuaException("登陆超时，请重新登陆");
        }*/
        // 头像的人脸检测
        try {
            byte[] bytes = headPhoto.getBytes();
            boolean detect = faceTemplate.detect(bytes);
            if(!detect) {
                // 不通过则报错
                throw new TanHuaException("没有检测到人脸，请重新上传");
            }
            log.info("人脸识别通过，准备上传到oss...........");
            // 通过则上传到阿里的oss
            String avatarUrl = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
            // 构建userInfo对象
            log.info("上传用户头像成功:{},{}",avatarUrl, UserHolder.getUserId());
            UserInfo userInfo = new UserInfo();
            userInfo.setId(UserHolder.getUserId());
            userInfo.setAvatar(avatarUrl);
            // 调用userInfoApi更新用户信息
            userInfoApi.update(userInfo);
        } catch (IOException e) {
            throw new TanHuaException("上传用户头像失败");
        }

    }

    /**
     * 通过用户id，查询用户详情
     * @param id
     * @return
     */
    public UserInfoVo findUserInfoById(Long id) {
        UserInfo userInfo = userInfoApi.findById(id);
        // 转成vo
        UserInfoVo vo = new UserInfoVo();
        // 复制属性值
        BeanUtils.copyProperties(userInfo,vo);
        // 年龄处理，如果数据库是空值
        Integer age = userInfo.getAge();
        if(age == null){
            // 默认18岁
            vo.setAge("18");
        }else{
            vo.setAge(age.toString());
        }
        return vo;
    }

    /**
     * 更新用户个人信息
     * @param token
     * @param vo
     */
    public void updateUserInfo(String token, UserInfoVo vo) {
        log.info("开始更新用户信息...........");
        // 获取登陆用户信息
        /*User user = getUserByToken(token);
        // 用户失效判断
        if(null == user) {
            // 失效则报错
            throw new TanHuaException("登陆超时，请重新登陆");
        }*/
        // 把vo转成userInfo
        UserInfo userInfo = new UserInfo();
        // 复制属性值
        BeanUtils.copyProperties(vo,userInfo);
        // 设置用户id
        userInfo.setId(UserHolder.getUserId());
        // 设置年龄
        userInfo.setAge(GetAgeUtil.getAge(vo.getBirthday()));
        // 更新
        userInfoApi.update(userInfo);
        log.info("更新用户信息成功,{}",userInfo);
    }

    /**
     * 【我的】更新头像
     * @param headPhoto
     * @param token
     */
    public void header(MultipartFile headPhoto, String token) {
        log.info("开始更新用户头像...........");
        // 获取登陆用户信息
        /*User user = getUserByToken(token);
        // 用户失效判断
        if(null == user) {
            // 失效则报错
            throw new TanHuaException("登陆超时，请重新登陆");
        }*/
        // 头像的人脸检测
        try {
            byte[] bytes = headPhoto.getBytes();
            boolean detect = faceTemplate.detect(bytes);
            if(!detect) {
                // 不通过则报错
                throw new TanHuaException("没有检测到人脸，请重新上传");
            }
            log.info("人脸识别通过，准备上传到oss...........");
            // 通过则上传到阿里的oss
            String avatarUrl = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
            // 构建userInfo对象
            log.info("上传用户头像成功:{},{}",avatarUrl, UserHolder.getUserId());
            // 先用户的头像信息
            UserInfo userInfo4Avatar = userInfoApi.findById(UserHolder.getUserId());
            // 旧的头像图片
            String oldAvatar = userInfo4Avatar.getAvatar();
            log.info("获取旧的头像图片成功....{}",oldAvatar);

            UserInfo userInfo = new UserInfo();
            userInfo.setId(UserHolder.getUserId());
            userInfo.setAvatar(avatarUrl);
            // 调用userInfoApi更新用户信息
            userInfoApi.update(userInfo);
            log.info("更新用户头像成功........");
            // 调用ossTemplate删除旧的头像图片，【最好使用MQ队列进行删除，异步调用】
            ossTemplate.deleteFile(oldAvatar);
            log.info("删除旧头像图片成功........{}",oldAvatar);
        } catch (IOException e) {
            throw new TanHuaException("上传用户头像失败");
        }
    }

    /**
     * 互相喜欢、喜欢、粉丝统计
     * @return
     */
    public CountsVo counts() {
        Long loginUserId = UserHolder.getUserId();
        // 互相喜欢
        Long eachLoveCount = userLikeApi.countLikeEachOther(loginUserId);
        // 喜欢
        Long loveCount = userLikeApi.countOneSideLike(loginUserId);
        // 粉丝
        Long fanCount = userLikeApi.countFens(loginUserId);

        CountsVo vo = new CountsVo();
        vo.setEachLoveCount(eachLoveCount);
        vo.setLoveCount(loveCount);
        vo.setFanCount(fanCount);
        return vo;
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
    public PageResult queryUserLikeList(int type, Long page, Long pagesize) {
        Long loginUserId = UserHolder.getUserId();
        // 根据类型来查询
        PageResult pageResult = new PageResult();
        boolean alreadyLove = false;
        switch (type){
            case 1:
                // 查询相互喜欢的
                pageResult = userLikeApi.findPageLikeEachOther(loginUserId,page,pagesize);
                alreadyLove = true;
                break;
            case 2:
                // 查询我喜欢的
                pageResult = userLikeApi.findPageOneSideLike(loginUserId,page,pagesize);
                alreadyLove = true;
                break;
            case 3:
                // 粉丝列表
                pageResult = userLikeApi.findPageFens(loginUserId,page,pagesize);
                alreadyLove = false;
                break;
            case 4:
                // 访客列表
                pageResult = userLikeApi.findPageMyVisitors(loginUserId,page,pagesize);
                break;
            default: break;
        }
        // 补全用户信息 RecommendUser
        // 获取分页结果集
        List<RecommendUser> recommendUserList = pageResult.getItems();
        // 遍历
        if(CollectionUtils.isNotEmpty(recommendUserList)) {
            List<FriendVo> voList = new ArrayList<>();
            for (RecommendUser recommendUser : recommendUserList) {
                // 获取用户的id 对方
                Long userId = recommendUser.getUserId();
                // 查询用户信息
                UserInfo userInfo = userInfoApi.findById(userId);
                // 转成vo
                FriendVo vo = new FriendVo();
                BeanUtils.copyProperties(userInfo, vo);
                vo.setAlreadyLove(alreadyLove);
                // 缘分值
                vo.setMatchRate(recommendUser.getScore().intValue());
                voList.add(vo);
            }
            // 设置回分页结果集
            pageResult.setItems(voList);
        }
        // 返回分页结果集
        return pageResult;
    }

    /**
     * 粉丝中的喜欢
     * @param fansId
     */
    public void fansLike(Long fansId) {
        Long loginUserId = UserHolder.getUserId();
        // 登陆用户来喜欢粉丝
        boolean flag = userLikeApi.fansLike(loginUserId,fansId);
        if(flag) {
            // 喜欢成功, 在环信上注册为好友
            huanXinTemplate.makeFriends(loginUserId, fansId);
        }
    }

    /**
     * 喜欢 取消
     * @param friendId
     */
    public void unlike(Long friendId) {
        Long loginUserId = UserHolder.getUserId();
        // 登陆用户来喜欢粉丝
        boolean flag = userLikeApi.unLikeUser(loginUserId,friendId);
        if(flag) {
            // 删除好友成功, 在环信上删除好友关系
            huanXinTemplate.removeFriend(loginUserId, friendId);
        }
    }

    /**
     * 判断登陆用户是否已喜欢
     * @param userId
     * @return
     */
    public boolean alreadyLove(Long userId) {
        return userLikeApi.alreadyLove(UserHolder.getUserId(),userId);
    }
}
