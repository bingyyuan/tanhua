package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 模拟序列，实现数值自动增长
 */
@Data
@Document(collection = "sequence")
public class Sequence {

    private ObjectId id;

    private long seqId; //自增序列的值

    private String collName;  //集合名称, 维护的是哪个表的序列
}