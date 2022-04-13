package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.Album;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 好友的服务提供者
 */
@Service
public class FriendApiImpl implements FriendApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 添加联系人
     * 互为好友，2条记录
     * @param loginUserId
     * @param userId
     */
    @Override
    public void add(Long loginUserId, Long userId) {
        // 判断登陆用户是否添加对方为好友, 方便测试，之前导入了测试数据，防止出错
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(loginUserId)
        .and("friendId").is(userId));
        if(!mongoTemplate.exists(query,Friend.class)) {
            // 构建friend对象
            Friend friend = new Friend();
            friend.setUserId(loginUserId);
            friend.setFriendId(userId);
            friend.setCreated(System.currentTimeMillis());
            // 添加记录
            mongoTemplate.insert(friend);
        }
        // 对方是否已经添加我为好友了
        Query query2 = new Query();
        query2.addCriteria(Criteria.where("userId").is(userId)
                .and("friendId").is(loginUserId));
        if(!mongoTemplate.exists(query2,Friend.class)) {
            // 对方添加我为好友
            Friend friend2 = new Friend();
            friend2.setUserId(userId);
            friend2.setFriendId(loginUserId);
            friend2.setCreated(System.currentTimeMillis());
            // 添加记录
            mongoTemplate.insert(friend2);
        }
    }

    /**
     * 联系人列表分页查询
     * @param userId 登陆用户id
     * @param page
     * @param pagesize
     * @param keyword
     * @return
     */
    @Override
    public PageResult findPage(Long userId, Long page, Long pagesize, String keyword) {
        // 构建查询条件，userId=登陆用户的id
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        Long total = mongoTemplate.count(query, Friend.class);
        // 构建pageResult
        PageResult pageResult = PageResult.total(total,page,pagesize);

        if(total>0){
            // 构建查询条件（分页，创建的时间降序)
            query.with(PageRequest.of(page.intValue()-1, pagesize.intValue()))
                    .with(Sort.by(Sort.Order.desc("created")));
            List<Friend> friendList = mongoTemplate.find(query, Friend.class);
            pageResult.setItems(friendList);
        }
        return pageResult;
    }
}
