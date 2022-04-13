package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.utils.IdService;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 小视频服务实现
 */
@Service
public class VideoApiImpl implements VideoApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;

    /**
     * 发布小视频
     *
     * @param video
     */
    @Override
    public void save(Video video) {
        video.setId(ObjectId.get());
        video.setVid(idService.nextId("video"));
        mongoTemplate.save(video);
    }

    /**
     * 小视频分页查询
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPage(Long page, Long pagesize) {
        Query query = new Query();
        Long total = mongoTemplate.count(query, Video.class);
        PageResult pageResult = PageResult.total(total, page, pagesize);

        // 分页查询
        query.with(PageRequest.of(page.intValue()-1,pagesize.intValue()))
                .with(Sort.by(Sort.Order.desc("created")));
        List<Video> videoList = mongoTemplate.find(query, Video.class);
        pageResult.setItems(videoList);
        return pageResult;
    }

    /**
     * 关注
     * @param followUser
     */
    @Override
    public void followUser(FollowUser followUser) {
        followUser.setId(ObjectId.get());
        mongoTemplate.save(followUser);
    }

    /**
     * 取消关注
     * @param followUser
     */
    @Override
    public void unfollowUser(FollowUser followUser) {
        // 删除的条件
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(followUser.getUserId())
        .and("followUserId").is(followUser.getFollowUserId()));

        // 删除
        mongoTemplate.remove(query,FollowUser.class);
    }
}
