package com.tanhua.commons.templates;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.properties.HuanXinProperties;
import com.tanhua.commons.vo.HuanXinUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
@Slf4j
public class HuanXinTemplate {

    @Autowired
    private RestTemplate restTemplate;

    private HuanXinProperties props;

    private long tokenDuration;

    private String token;

    public HuanXinTemplate(HuanXinProperties properties){
        this.props = properties;
    }

    /**
     * 发送短信
     * @param target 接收方的id
     * @param msg
     */
    public void sendMsg(String target, String msg) {
        String url = props.getHuanXinUrl();
        url+="/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");
        headers.add("Authorization","Bearer " + getToken());

        // 请求体
        Map<String,Object> requestBody = new HashMap<String,Object>();
        requestBody.put("target_type","users");
        requestBody.put("target", Arrays.asList(target));

        Map<String, Object> msgParam = new HashMap<String, Object>();
        msgParam.put("msg", msg);
        msgParam.put("type", "txt");

        requestBody.put("msg", msgParam);

        HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<Map<String,Object>>(requestBody,headers);
        // 发送请求
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        try {
            int statusCode = responseEntity.getStatusCode().value();
            log.info("发送消息*****url:{}******sendMsg:{}******statusCode{}*****",url,msg,statusCode);
            if(200 != statusCode){
                log.info("发送消息失败，状态码不为200::::::::::::{}",responseEntity);
                throw new TanHuaException("发送信息失败!");
            }
        } catch (Exception e) {
            //e.printStackTrace();
            log.error("发送信息失败,发生异常",e);
        }

    }

    /**
     * 交友
     * @param userId
     * @param friendId
     */
    public void makeFriends(Long userId, Long friendId){
        String token = getToken();
        String url = props.getHuanXinUrl();
        ///users/{owner_username}/contacts/users/{friend_username}
        url+="/users/"+ userId.toString()+"/contacts/users/" + friendId.toString();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");
        headers.add("Authorization","Bearer " + token);

        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        int statusCode = responseEntity.getStatusCode().value();
        log.info("交友*****url:{}******userId:{}******friendId{}*****",url,userId,friendId);
        if(200 != statusCode){
            throw new TanHuaException("添加好友失败!");
        }
    }

    /**
     * 移除好友
     * @param userId
     * @param friendId
     */
    public void removeFriend(Long userId, Long friendId) {
        String token = getToken();
        String url = props.getHuanXinUrl();
        ///users/{owner_username}/contacts/users/{friend_username}
        url+="/users/"+ userId.toString()+"/contacts/users/" + friendId.toString();
        URI deleteUri = URI.create(url);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");
        headers.add("Authorization","Bearer " + token);

        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(deleteUri, HttpMethod.DELETE,httpEntity,String.class);
        int statusCode = responseEntity.getStatusCode().value();
        log.info("删除好友*****url:{}******userId:{}******friendId{}*****",url,userId,friendId);
        if(404 != statusCode || 200 != statusCode){
            throw new TanHuaException("删除好友失败!");
        }
    }

    /**
     * 注册用户
     * @param userId
     */
    public void register(Long userId){
        // 获取授权令牌
        String token = getToken();
        // 注册用户rest api地址
        String url = props.getHuanXinUrl();
        url+="/users";
        // 请求体内容
        HuanXinUser user = new HuanXinUser(userId.toString(),"123456","1");
        // 请求头信息
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");
        headers.add("Authorization","Bearer " + token);
        // 发送请求
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, new HttpEntity<HuanXinUser>(user, headers), String.class);
        int statusCode = responseEntity.getStatusCode().value();
        log.info("注册用户*****url:{}******userId:{}*****",url,userId);
        if(200 != statusCode){
            throw new TanHuaException("用户注册失败,HuanXin httpCode:"+statusCode);
        }
    }

    /**
     * 注册用户
     * 解决数据库已有用户，而环信上没有的
     */
    public void registerBatch(){
        // 获取授权令牌
        String token = getToken();
        // 注册用户rest api地址
        String url = props.getHuanXinUrl();
        url+="/users";
        // 请求体内容
        HuanXinUser user = new HuanXinUser("1","123456","1");
        List<HuanXinUser> list = new ArrayList<HuanXinUser>();
        for (int i = 10; i < 20 ; i++) {
            list.add(new HuanXinUser(i+"",i+"",String.format("今晚打老虎_%05d",i)));
        }
        // 请求头信息
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json");
        headers.add("Authorization","Bearer " + token);
        // 发送请求
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, new HttpEntity<List>(list, headers), String.class);
        int statusCode = responseEntity.getStatusCode().value();
        log.info("注册用户*****url:{}******userId:{}*****",url,list);
        if(200 != statusCode){
            throw new TanHuaException("用户注册失败,HuanXin httpCode:"+statusCode);
        }
    }

    /**
     * 获取授权
     * @return
     */
    private String getToken(){
        if(tokenDuration < System.currentTimeMillis()){
            return applyNewToken();
        }
        return token;
    }

    /**
     * 获取管理员的授权令牌
     * @return
     */
    private String applyNewToken(){
        //http://a1.easemob.com/token
        String url = props.getHuanXinUrl();
        url+="/token";

        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("grant_type","client_credentials");
        paramMap.put("client_id",props.getClientId());
        paramMap.put("client_secret",props.getClientSecret());

        ResponseEntity<String> resEntity = restTemplate.postForEntity(url, paramMap, String.class);
        int statusCode = resEntity.getStatusCode().value();
        log.info("获取管理员的授权令牌*****url:{}******statusCode:{}*****",url,statusCode);
        if(200 != statusCode){
            throw new TanHuaException("获取环信token失败");
        }
        Map<String,Object> resultMap = JSON.parseObject(resEntity.getBody(),Map.class);
        long expiresInSeconds = (int)resultMap.get("expires_in");//token的有期
        tokenDuration = System.currentTimeMillis()-10*60*1000 + expiresInSeconds*1000; // 失效时间 前10分钟就会获取新的token
        token = (String) resultMap.get("access_token");
        return token;
    }
}