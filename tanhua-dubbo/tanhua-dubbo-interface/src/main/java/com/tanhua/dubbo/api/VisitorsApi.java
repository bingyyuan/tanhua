package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.Visitor;

import java.util.List;

public interface VisitorsApi {

    /**
     * 查询最近访客记录
     * @param userId
     * @param lastTime
     * @return
     */
    List<Visitor> queryVisitors(Long userId, String lastTime);

    /**
     * 添加访问记录
     * @param visitor
     */
    void save(Visitor visitor);
}
