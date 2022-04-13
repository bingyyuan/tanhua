package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.Voice;

public interface VoiceApi {

    /**
     * 桃花传音：发送语音
     * @param loginUserId
     * @param voiceUrl
     */
    void add(Long loginUserId, String voiceUrl);

    Voice findOne();
}
