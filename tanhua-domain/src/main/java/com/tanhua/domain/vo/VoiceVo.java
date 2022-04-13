package com.tanhua.domain.vo;

import lombok.Data;

@Data
public class VoiceVo {
    private Long id;
    private String avatar;
    private String nickname;
    private String gender;
    private Integer age;
    private String soundUrl; // 语音地址
    private int remainingTimes; // 剩余次数
}
