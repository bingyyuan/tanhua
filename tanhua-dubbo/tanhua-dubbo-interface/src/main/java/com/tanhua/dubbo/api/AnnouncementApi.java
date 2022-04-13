package com.tanhua.dubbo.api;

import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;

public interface AnnouncementApi {

    /**
     * 公告列表
     * @param page
     * @param size
     * @return
     */
    PageResult<Announcement> findPage(Integer page, Integer size);
}
