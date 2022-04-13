package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.Voice;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@Service
public class VoiceApiImpl implements VoiceApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 桃花传音：发送语音
     *
     * @param loginUserId
     * @param voiceUrl
     */
    @Override
    public void add(Long loginUserId, String voiceUrl) {
        Voice voice = new Voice();
        voice.setCreated(System.currentTimeMillis());
        voice.setUserId(loginUserId);
        voice.setSoundUrl(voiceUrl);
        mongoTemplate.insert(voice);
    }

    /**
     * 桃花传音：接收语音
     * @return
     */
    @Override
    public Voice findOne() {
        Query query = new Query();
        long count = mongoTemplate.count(query, Voice.class);
        long skipped = RandomUtils.nextLong(0, count);
        query.with(Sort.by(Sort.Order.desc("created")));
        query.skip(skipped).limit(1);
        return mongoTemplate.findOne(query,Voice.class);
    }
}
