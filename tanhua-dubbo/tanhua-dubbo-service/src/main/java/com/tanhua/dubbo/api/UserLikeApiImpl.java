package com.tanhua.dubbo.api;

import com.tanhua.domain.db.User;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.UserLike;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReactiveSessionCallback;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserLikeApiImpl implements UserLikeApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FriendApi friendApi;

    /**
     * 互相喜欢数量
     * user_friend userId=登陆用户id
     * @param loginUserId
     * @return
     */
    @Override
    public Long countLikeEachOther(Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        return mongoTemplate.count(query, Friend.class);
    }

    /**
     * 我喜欢数量
     * user_like userId=登陆用户 id
     * @return
     */
    @Override
    public Long countOneSideLike(Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * 喜欢我的人数
     * likeUserId=登陆用户 id
     * @return
     */
    @Override
    public Long countFens(Long loginUserId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * 相互喜欢列表
     * user_friend
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return RecommendUser
     */
    @Override
    public PageResult findPageLikeEachOther(Long loginUserId, Long page, Long pagesize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        Long total = mongoTemplate.count(query, Friend.class);
        // 构建pageResult
        PageResult pageResult = PageResult.total(total,page,pagesize);

        if(total>0){
            // 构建查询条件（分页，创建的时间降序)
            query.with(PageRequest.of(page.intValue()-1, pagesize.intValue()))
                    .with(Sort.by(Sort.Order.desc("created")));
            List<Friend> friendList = mongoTemplate.find(query, Friend.class);
            List<RecommendUser> list = new ArrayList<>();
            // 遍历好友，查询分数且转成recommendUser
            for (Friend friend : friendList) {
                list.add(queryScore(friend.getFriendId(),loginUserId));
            }
            pageResult.setItems(list);
        }
        return pageResult;
    }

    /**
     * 我喜欢的列表
     *
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPageOneSideLike(Long loginUserId, Long page, Long pagesize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));
        Long total = mongoTemplate.count(query, UserLike.class);
        // 构建pageResult
        PageResult pageResult = PageResult.total(total,page,pagesize);

        if(total>0){
            // 构建查询条件（分页，创建的时间降序)
            query.with(PageRequest.of(page.intValue()-1, pagesize.intValue()))
                    .with(Sort.by(Sort.Order.desc("created")));
            List<UserLike> myLoveList = mongoTemplate.find(query, UserLike.class);
            List<RecommendUser> list = new ArrayList<>();
            // 遍历我的喜欢的列表，查询分数且转成recommendUser
            for (UserLike myLove : myLoveList) {
                list.add(queryScore(myLove.getLikeUserId(),loginUserId));
            }
            pageResult.setItems(list);
        }
        return pageResult;
    }

    /**
     * 我的粉丝列表
     *
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPageFens(Long loginUserId, Long page, Long pagesize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId));
        Long total = mongoTemplate.count(query, UserLike.class);
        // 构建pageResult
        PageResult pageResult = PageResult.total(total,page,pagesize);

        if(total>0){
            // 构建查询条件（分页，创建的时间降序)
            query.with(PageRequest.of(page.intValue()-1, pagesize.intValue()))
                    .with(Sort.by(Sort.Order.desc("created")));
            List<UserLike> myLoveList = mongoTemplate.find(query, UserLike.class);
            List<RecommendUser> list = new ArrayList<>();
            // 遍历我的喜欢的列表，查询分数且转成recommendUser
            for (UserLike myLove : myLoveList) {
                list.add(queryScore(myLove.getUserId(),loginUserId));
            }
            pageResult.setItems(list);
        }
        return pageResult;
    }

    /**
     * 访客列表
     *
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPageMyVisitors(Long loginUserId, Long page, Long pagesize) {
        //- 查询visitors条件userId=登陆用户id
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId));

        Long total = mongoTemplate.count(query, Visitor.class);
        PageResult pageResult = PageResult.total(total, page, pagesize);
        if(total>0) {
            // 最近的，时间降序
            query.with(Sort.by(Sort.Order.desc("date")));
            query.with(PageRequest.of(page.intValue()-1,pagesize.intValue()));
            List<Visitor> visitors = mongoTemplate.find(query, Visitor.class);

            // ============= 查询访客的缘分值 ===================
            List<RecommendUser> list = new ArrayList<>();
            // 遍历我的喜欢的列表，查询分数且转成recommendUser
            for (Visitor visitor : visitors) {
                list.add(queryScore(visitor.getVisitorUserId(),loginUserId));
            }
            pageResult.setItems(list);
        }
        return pageResult;
    }

    /**
     * 登陆用户喜欢粉丝
     * @param loginUserId
     * @param fansId
     * @return
     */
    @Override
    public boolean fansLike(Long loginUserId, Long fansId) {
        // 先判断对方是否也喜欢我
        Query query = new Query();
        query.addCriteria(Criteria.where("likeUserId").is(loginUserId).and("userId").is(fansId));
        if(mongoTemplate.exists(query,UserLike.class)) {
            // 是 则可以交友,true
            // 删除粉丝的喜欢的记录
            mongoTemplate.remove(query,UserLike.class);
            // 添加好友记录
            friendApi.add(loginUserId,fansId);
            return true;
        }else {
            // 不是，单方喜欢，添加喜欢记录
            UserLike userLike = new UserLike();
            userLike.setLikeUserId(fansId);
            userLike.setUserId(loginUserId);
            userLike.setCreated(System.currentTimeMillis());
            mongoTemplate.insert(userLike);
        }
        return false;
    }

    /**
     * 查询推荐用户的分数
     * @param targetUserId
     * @param loginUserId
     * @return
     */
    private RecommendUser queryScore(Long targetUserId,Long loginUserId){
        Query recommendUserQuery = new Query();
        recommendUserQuery.addCriteria(Criteria.where("toUserId").is(loginUserId).and("userId").is(targetUserId));
        // 访客的推荐信息
        RecommendUser recommendUser = mongoTemplate.findOne(recommendUserQuery, RecommendUser.class);
        // 给访客设置缘分值
        if(null == recommendUser){
            recommendUser = new RecommendUser();
            recommendUser.setUserId(targetUserId);
            recommendUser.setToUserId(loginUserId);
            recommendUser.setScore(70d);
        }
        return recommendUser;
    }

    /**
     * 添加喜欢
     * @param userId
     * @param targetUserId
     */
    @Override
    public void likeUser(Long userId, Long targetUserId) {
        fansLike(userId,targetUserId);
    }

    /**
     * 取消喜欢
     * @param loginUserId
     * @param friendId
     */
    @Override
    public boolean unLikeUser(Long loginUserId, Long friendId) {
        // 我是否与对方为好友
        Query query = new Query();
        boolean flag = false; // 是否需要添加对方是登陆用户的粉丝
        query.addCriteria(Criteria.where("userId").is(loginUserId).and("friendId").is(friendId));
        if(mongoTemplate.exists(query,Friend.class)){
            // 如果是好友，则删除我与他的好友关系
            mongoTemplate.remove(query,Friend.class);
            flag = true;
        }

        // 从好友的角度，登陆用户是否是对方的好友
        query = new Query();
        query.addCriteria(Criteria.where("userId").is(friendId).and("friendId").is(loginUserId));
        if(mongoTemplate.exists(query,Friend.class)){
            // 如果是好友，则删除对方与登陆用户的好友关系
            mongoTemplate.remove(query,Friend.class);
            flag = true;
        }
        query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId).and("likeUserId").is(friendId));
        // 检查是否登陆用户是否喜欢对方
        if(mongoTemplate.exists(query, UserLike.class)) {
            // 如果喜欢对方，则删除关系
            mongoTemplate.remove(query, UserLike.class);
        }

        // 判断是否存在对方是喜欢自己的，如果不存在，则添加
        query = new Query();
        query.addCriteria(Criteria.where("userId").is(friendId).and("likeUserId").is(loginUserId));
        if(flag && !mongoTemplate.exists(query,UserLike.class)){
            UserLike userLike = new UserLike();
            userLike.setUserId(friendId);
            userLike.setLikeUserId(loginUserId);
            userLike.setCreated(System.currentTimeMillis());
            mongoTemplate.insert(userLike);
        }
        return flag;
    }

    /**
     * 判断登陆用户是否已喜欢
     * @param loginUserId
     * @param userId
     * @return
     */
    @Override
    public boolean alreadyLove(Long loginUserId, Long userId) {
        Query query = new Query();
        boolean flag = false; // 是否需要添加对方是登陆用户的粉丝
        query.addCriteria(Criteria.where("userId").is(loginUserId).and("friendId").is(userId));
        if(mongoTemplate.exists(query,Friend.class)){
            // 如果是好友，则删除我与他的好友关系
            flag = true;
        }else{
            query = new Query();
            query.addCriteria(Criteria.where("userId").is(loginUserId).and("likeUserId").is(userId));
            // 检查是否登陆用户是否喜欢对方
            if(mongoTemplate.exists(query, UserLike.class)) {
                flag = true;
            }
        }
        return flag;
    }

}
