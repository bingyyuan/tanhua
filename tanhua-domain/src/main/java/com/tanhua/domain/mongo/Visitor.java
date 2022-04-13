package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 访客实体类
 */
@Data
@Document(collection = "visitors")
public class Visitor implements Serializable {

    private ObjectId id;
    private Long userId; //我的id
    private Long visitorUserId; //来访用户id
    private String from; //来源，如首页、圈子、小视频等
    private Long date; //来访时间

    private Double score; //得分
}