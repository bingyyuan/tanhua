package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "lucky_voice")
public class Voice implements Serializable {
    @Id
    private ObjectId id;
    private Long userId; // 发布者id
    private String soundUrl; // 语音连接地址
    private Long created; // 创建时间
}
