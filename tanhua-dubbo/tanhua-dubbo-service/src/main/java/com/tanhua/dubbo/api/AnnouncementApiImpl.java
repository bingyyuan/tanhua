package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.Announcement;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.AnnouncementMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2020/12/16
 */
@Service
public class AnnouncementApiImpl implements AnnouncementApi {

    @Autowired
    private AnnouncementMapper announcementMapper;

    /**
     * 公告列表查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Announcement> findPage(Integer page, Integer size) {
        PageResult<Announcement> pageResult = new PageResult<Announcement>();
        pageResult.setPage((long)page);
        pageResult.setPagesize((long)size);
        Page<Announcement> pageInfo = new Page<Announcement>(page,size);
        IPage<Announcement> iPage = announcementMapper.selectPage(pageInfo, null);
        pageResult.setCounts(iPage.getTotal());
        pageResult.setItems(iPage.getRecords());
        return pageResult;
    }
}
