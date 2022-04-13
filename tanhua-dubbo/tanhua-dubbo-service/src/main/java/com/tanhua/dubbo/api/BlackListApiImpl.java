package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.BlackListMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/7
 */
@Service
public class BlackListApiImpl implements BlackListApi {

    @Autowired
    private BlackListMapper blackListMapper;

    /**
     * 分页查询
     *
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult findBlackList(long page, long pagesize, Long userId) {
        // 构建page对象
        IPage<UserInfo> userInfoPage = new Page<UserInfo>(page,pagesize);
        // 查询
        IPage<UserInfo> page2 = blackListMapper.findBlackList(userInfoPage,userId);
        // 构建PageResult，
        PageResult pageResult = new PageResult();
        // 总记录数， 前端需要
        pageResult.setCounts(page2.getTotal());
        // 每页大小
        pageResult.setPagesize(pagesize);
        // 总页数
        pageResult.setPages(page2.getPages());
        // 分页的结果集
        pageResult.setItems(page2.getRecords());
        // 当前页码
        pageResult.setPage(page);
        return pageResult;
    }

    /**
     * 移除黑名单
     * @param userId
     * @param uid
     */
    @Override
    public void delete(Long userId, long uid) {
        // 构建条件
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<BlackList>();
        // where user_id=userId and black_user_id=uid
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("black_user_id",uid);
        blackListMapper.delete(queryWrapper);
    }
}
