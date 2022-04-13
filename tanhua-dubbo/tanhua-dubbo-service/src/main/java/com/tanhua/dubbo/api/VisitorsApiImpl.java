package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Visitor;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * 访客服务提供者
 */
@Service
public class VisitorsApiImpl implements VisitorsApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询最近访客记录
     *
     * @param userId 登陆用户id
     * @param lastTime 上次的时间
     * @return
     */
    @Override
    public List<Visitor> queryVisitors(Long userId, String lastTime) {
        //- 查询visitors条件userId=登陆用户id
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        //- 有时间，date>上次的时间，如果没有时间，则不需要条件
        if(StringUtils.isNotEmpty(lastTime)){
            // 转换类型
            Long date = Long.valueOf(lastTime);
            // date>=记录的时间
            query.addCriteria(Criteria.where("date").gte(date));
        }
        // 最近的，时间降序
        query.with(Sort.by(Sort.Order.desc("date")));
        //- 取5条
        query.limit(5);
        List<Visitor> visitors = mongoTemplate.find(query, Visitor.class);

        // ============= 查询访客的缘分值 ===================
        //- 遍历访客的id, 查询访客的缘分值recommendUser（默认值70），补充到实体类中，再返回
        if(CollectionUtils.isNotEmpty(visitors)){
            for (Visitor visitor : visitors) {
                // 访客的id
                Long visitorUserId = visitor.getVisitorUserId();
                Query recommendUserQuery = new Query();
                recommendUserQuery.addCriteria(Criteria.where("toUserId").is(userId).and("userId").is(visitorUserId));
                // 访客的推荐信息
                RecommendUser recommendUser = mongoTemplate.findOne(recommendUserQuery, RecommendUser.class);
                // 给访客设置缘分值
                if(null == recommendUser){
                    visitor.setScore(70d);
                }else{
                    visitor.setScore(recommendUser.getScore());
                }
            }
        }
        return visitors;
    }

    /**
     * 保存访客记录
     */
    @Override
    public void save(Visitor visitor) {
        visitor.setDate(System.currentTimeMillis());
        mongoTemplate.save(visitor);
    }
}
