package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.utils.IdService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 动态服务
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/10
 */
@Service
@Slf4j
public class PublishApiImpl implements PublishApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;

    /**
     * 发布动态的mongodb操作
     * @param publish
     */
    @Override
    public void add(Publish publish) {
        // 统一时间
        long timeMillis = System.currentTimeMillis();
        // ================ 动态表 publish ==================
        //4. 填充数据, 生成ObjectId, publish的id
        ObjectId publishId = publish.getId();
        log.info("publishId={}, userId={}",publishId, publish.getUserId());
        publish.setId(publishId);
        // 给推荐系统使用的，不理
        publish.setPid(idService.nextId("quanzi_publish"));
        publish.setSeeType(1); //1:公开
        publish.setCreated(timeMillis);
        publish.setState(1); //1：审核通过
        mongoTemplate.insert(publish);

        // ================== 我的相册 =====================
        //6. 构建相册对象，且填充值
        Album album = new Album();
        album.setId(ObjectId.get());
        album.setPublishId(publishId);
        album.setCreated(timeMillis);
        //保存相册,指定集合名称 每人一张表
        mongoTemplate.insert(album,"quanzi_album_" + publish.getUserId());

        // ============= 好友时间线表 ========================
        // 查询好友的id集合
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(publish.getUserId()));
        List<Friend> friends = mongoTemplate.find(query, Friend.class);
        if(null != friends) {
            //9. 遍历好友，添加好友的时间线(构建timeline对象),指定集合名称 每人一张表
            // 【注意】day11 rocketMq 来实现
            for (Friend friend : friends) {
                // 好友的时间线表名
                String collectionName = "quanzi_time_line_" + friend.getFriendId();
                // 构建时间线对象
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());
                // 创建的时间，
                timeLine.setCreated(timeMillis);
                // 动态的id
                timeLine.setPublishId(publishId);
                // 我是他的好友
                timeLine.setUserId(publish.getUserId());
                mongoTemplate.insert(timeLine,collectionName);
            }
        }
    }

    /**
     * 通过用户id分页查询好友动态
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findFriendPublishByTimeline(Long page, Long pagesize,Long userId) {
        // 拼接时间线表
        String collectionName = "quanzi_time_line_" + userId;
        Query query = new Query();
        // 总计录数
        Long total = mongoTemplate.count(query, collectionName);
        PageResult pageResult = new PageResult();
        pageResult.setPage(page.longValue());
        pageResult.setPagesize(pagesize.longValue());
        // 总页面
        long pages = total/pagesize;
        if(total.intValue()%pagesize>0){
            pages+=1;
        }
        //pages+=total.intValue()%pagesize>0?1:0;
        pageResult.setPages(pages);
        pageResult.setCounts(total);
        // 分页查询
        if(total>0) {
            // 按创建时间降序，且分页
            query.with(Sort.by(Sort.Order.desc("created"))).with(PageRequest.of(page.intValue() - 1, pagesize.intValue()));
            List<TimeLine> timeLines = mongoTemplate.find(query, TimeLine.class, collectionName);
            // 获取动态id集合, // 把集合timeLines中 每个元素 取出getPublishId, 放到一个list里
            List<ObjectId> publishIds = timeLines.stream().map(TimeLine::getPublishId).collect(Collectors.toList());
            // 通过动态id查询动态信息
            Query publishQuery = new Query();
            publishQuery.addCriteria(Criteria.where("_id").in(publishIds));
            // 查询得到动态信息集合
            List<Publish> publishes = mongoTemplate.find(publishQuery, Publish.class);
            pageResult.setItems(publishes);
        }
        return pageResult;
    }

    /**
     * 查询推荐动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult findRecommendPublish(Long page, Long pagesize, Long userId) {
        Query query = new Query();
        // 总计录数
        Long total = mongoTemplate.count(query, RecommendQuanzi.class);
        PageResult pageResult = new PageResult();
        pageResult.setPage(page.longValue());
        pageResult.setPagesize(pagesize.longValue());
        // 总页面
        long pages = total/pagesize;
        if(total.intValue()%pagesize>0){
            pages+=1;
        }
        //pages+=total.intValue()%pagesize>0?1:0;
        pageResult.setPages(pages);
        pageResult.setCounts(total);
        // 分页查询
        if(total>0) {
            // 按创建时间降序，且分页
            query.with(Sort.by(Sort.Order.desc("score"))).with(PageRequest.of(page.intValue() - 1, pagesize.intValue()));
            List<RecommendQuanzi> recommendQuanzis = mongoTemplate.find(query, RecommendQuanzi.class);
            // 获取动态id集合, // 把集合recommendQuanzis中 每个元素 取出getPublishId, 放到一个list里
            List<ObjectId> publishIds = recommendQuanzis.stream().map(RecommendQuanzi::getPublishId).collect(Collectors.toList());
            // 通过动态id查询动态信息
            Query publishQuery = new Query();
            publishQuery.addCriteria(Criteria.where("_id").in(publishIds));
            // 查询得到动态信息集合
            List<Publish> publishes = mongoTemplate.find(publishQuery, Publish.class);
            pageResult.setItems(publishes);
        }
        return pageResult;
    }

    /**
     * 查询我的动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult findMyAlbum(Long page, Long pagesize, Long userId) {
        // 查询我的相册表, 拼接表名
        String myAlbumName = "quanzi_album_" + userId;
        // 统计总记录
        Query query = new Query();
        Long total = mongoTemplate.count(query, myAlbumName);
        // 构建pageResult
        PageResult pageResult = PageResult.total(total,page,pagesize);

        if(total>0){
            // 构建查询条件（分页，创建的时间降序)
            query.with(PageRequest.of(page.intValue()-1, pagesize.intValue()))
                .with(Sort.by(Sort.Order.desc("created")));
            List<Album> albums = mongoTemplate.find(query, Album.class, myAlbumName);
            // 获取动态的id集合
            List<ObjectId> publishIds = albums.stream().map(Album::getPublishId).collect(Collectors.toList());
            // 通过id集合查询动态信息
            Query publishQuery = new Query();
            publishQuery.addCriteria(Criteria.where("_id").in(publishIds));
            List<Publish> publishes = mongoTemplate.find(publishQuery, Publish.class);
            pageResult.setItems(publishes);
        }
        return pageResult;
    }

    /**
     * 通过id查询动态信息
     * @param publishId
     * @return
     */
    @Override
    public Publish findById(String publishId) {
        return mongoTemplate.findById(new ObjectId(publishId),Publish.class);
    }

    /**
     * 更新动态的状态
     * @param publish
     */
    @Override
    public void updateState(Publish publish) {
        // 条件
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(publish.getId()));
        // 更新的字段
        Update update = new Update();
        update.set("state",publish.getState());

        mongoTemplate.updateFirst(query,update,Publish.class);
    }
}
