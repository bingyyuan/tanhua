package com.tanhua.server.service;

import cn.hutool.core.date.DateUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Voice;
import com.tanhua.domain.vo.VoiceVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VoiceApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Calendar;

@Service
@Slf4j
public class VoiceService {

    @Reference
    private VoiceApi voiceApi;

    @Autowired
    private FastFileStorageClient fastDFSClient;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private RedisTemplate redisTemplate;

    //StringRedisTemplate => RedisTemplate<String,String>

    @Value("${tanhua.luckyVoiceCount}")
    private int luckyVoiceCount;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * ???????????????????????????
     * @param soundFile
     */
    public void save(MultipartFile soundFile) {
        Long loginUserId = UserHolder.getUserId();
        // ???????????????????????????
        String filename = soundFile.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf(".")+1);
        // ???????????????fast dfs
        StorePath storePath = null;
        try {
            storePath = fastDFSClient.uploadFile(soundFile.getInputStream(), soundFile.getSize(), suffix, null);
        } catch (IOException e) {
            //e.printStackTrace();
            throw new TanHuaException("????????????????????????!");
        }
        String voiceUrl = fdfsWebServer.getWebServerUrl() + storePath.getFullPath();
        voiceApi.add(loginUserId,voiceUrl);
    }

    public VoiceVo findOne() {
        Long loginUserId = UserHolder.getUserId();
        Voice voice = voiceApi.findOne();
        VoiceVo vo = new VoiceVo();
        BeanUtils.copyProperties(voice,vo);

        UserInfo userInfo = userInfoApi.findById(voice.getUserId());
        BeanUtils.copyProperties(userInfo,vo);

        String today = DateUtil.today();
        String key = "lucky_voice_count_" + today + "_" + loginUserId;
        // redis?????????????????????????????????
        // redisTemplate.opsForValue();// ????????????????????????(key:jdk,value:jdk)
        // stringCommands?????????????????????????????????redis???api??????
        RedisStringCommands commands = redisTemplate.getConnectionFactory().getConnection().stringCommands();
        //redisTemplate.expireAt(key,???????????????)
        // ?????????????????????
        StringRedisSerializer serializer = new StringRedisSerializer();
        byte[] keyBytes = serializer.serialize(key);
        String valueInRedis = serializer.deserialize(commands.get(keyBytes));

        int count = luckyVoiceCount;
        if(null != valueInRedis){
            count = Integer.parseInt(valueInRedis);
        }else{
            Calendar car = Calendar.getInstance();
            car.set(Calendar.HOUR_OF_DAY,23);
            car.set(Calendar.MINUTE,59);
            car.set(Calendar.SECOND,59);
            // ?????????????????????????????????????????????
            long secondsRemainsInToday = (car.getTimeInMillis() - System.currentTimeMillis()) / 1000 + 1;
            // ??????redis??????
            commands.set(keyBytes,serializer.serialize(String.valueOf(count)),
                    // ?????????????????????????????????
                    Expiration.seconds(secondsRemainsInToday), RedisStringCommands.SetOption.UPSERT);
        }

        if(count > 0){
            // ???1
            commands.decr(keyBytes);
        }
        vo.setRemainingTimes(count - 1);
        return vo;
    }

}
