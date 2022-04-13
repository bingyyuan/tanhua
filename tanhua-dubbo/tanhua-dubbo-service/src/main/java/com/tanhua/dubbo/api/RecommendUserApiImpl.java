package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.List;


/**
 * <p>
 * 好友推荐服务实现,
 * 查询是mongodb
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/9
 */
@Service
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询佳人，缘分值最高的
     * @param loginUserId
     * @return
     */
    @Override
    public RecommendUser queryWithMaxScore(Long loginUserId) {
        Query query = new Query();
        // 推荐给登陆用户的id
        query.addCriteria(Criteria.where("toUserId").is(loginUserId));
        // 按分数降序, 只取1个
        // 按哪个字段，怎么排序
        Sort.Order score = Sort.Order.desc("score");
        Sort sort = Sort.by(score);
        // 排序获取第1个
        query.with(sort).limit(1);
        RecommendUser jiaren = mongoTemplate.findOne(query, RecommendUser.class);
        return jiaren;
    }

    /**
     * 首页推荐用户分页查询
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult findPage(Integer page, Integer pagesize, Long userId) {
        Query query = new Query();
        // where toUserId=userId
        query.addCriteria(Criteria.where("toUserId").is(userId));
        // 获取总记录数
        Long total = mongoTemplate.count(query, RecommendUser.class);
        // 分页的结果集
        List<RecommendUser> list = Collections.EMPTY_LIST;
        if(total > 0){
            // 有记录才
            // 分页
            query.with(PageRequest.of(page-1,pagesize))
                // 按score降序排序
                .with(Sort.by(Sort.Order.desc("score")));
            list = mongoTemplate.find(query, RecommendUser.class);
        }
        PageResult pageResult = new PageResult();
        pageResult.setItems(list);
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

        return pageResult;
    }

    /**
     * 查询推荐用户的缘分值
     * @param loginUserId 登陆用户
     * @param userId 佳人id
     * @return
     */
    @Override
    public Double queryForScore(Long loginUserId, Long userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("toUserId").is(loginUserId)
        .and("userId").is(userId));

        RecommendUser user = mongoTemplate.findOne(query, RecommendUser.class);
        if(null == user){
            // 默认缘分值
            return 95d;
        }
        return user.getScore();
    }
}
