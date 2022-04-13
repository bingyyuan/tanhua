package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.Comment;
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
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * <p>
 *  点赞、评论、喜欢的服务
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/11
 */
@Service
public class CommentApiImpl implements CommentApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 动态点赞
     *
     * @param comment
     * @return 最新点赞数
     */
    @Override
    public int save(Comment comment) {
        // 通过动态的id查询动态信息,获取动态的作者id
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(comment.getPublishId()));
        Publish publish = mongoTemplate.findOne(query, Publish.class);
        comment.setPublisherId(publish.getUserId());
        // 保存评论信息
        mongoTemplate.save(comment);
        // 更新动态中的点赞数
        Update update = new Update();
        update.inc(comment.getCol(),1);
        mongoTemplate.updateFirst(query,update,Publish.class);
        // 返回最新的点赞数
        publish = mongoTemplate.findOne(query, Publish.class);
        // 喜欢的数量
        if(comment.getCommentType() == 3){
            return publish.getLoveCount();
        }
        // 评论数量
        if(comment.getCommentType() == 2){
            return publish.getCommentCount();
        }
        // 默认使用点赞数量
        return publish.getLikeCount();
    }

    /**
     * 取消点赞
     * @param comment
     * @return
     */
    @Override
    public int remove(Comment comment) {
        Query query = new Query();
        // 构建删除的条件
        query.addCriteria(Criteria
            .where("userId").is(comment.getUserId())
            .and("commentType").is(comment.getCommentType())
            .and("publishId").is(comment.getPublishId())
        );
        mongoTemplate.remove(query,Comment.class);
        // 点赞数据要-1
        Update update = new Update();
        update.inc(comment.getCol(),-1);
        Query publishQuery = new Query();
        publishQuery.addCriteria(Criteria.where("_id").is(comment.getPublishId()));
        // 更新 update publish set lickCount=likeCount-1 where _id=getPublishId
        // 条件满足时只更新第一条
        mongoTemplate.updateFirst(publishQuery,update,Publish.class);

        // 获取最新的点赞数
        Publish publish = mongoTemplate.findOne(publishQuery, Publish.class);
        // 喜欢的数量
        if(comment.getCommentType() == 3){
            return publish.getLoveCount();
        }
        return publish.getLikeCount();
    }

    /**
     * 动态的评论列表
     * @param page
     * @param pagesize
     * @param publishId
     * @return
     */
    @Override
    public PageResult findPage(Long page, Long pagesize, String publishId) {
        // 构建条件
        Query query = new Query();
        query.addCriteria(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(2));// 评论的
        // 统计总数
        Long total = mongoTemplate.count(query, Comment.class);
        PageResult pageResult = PageResult.total(total, page, pagesize);
        // 总数>0
        if(total > 0) {
            // 分页查询
            query.with(PageRequest.of(page.intValue()-1,pagesize.intValue()))
                    .with(Sort.by(Sort.Order.desc("created")));
            List<Comment> comments = mongoTemplate.find(query, Comment.class);
            pageResult.setItems(comments);
        }

        return pageResult;
    }

    /**
     * 对评论点赞
     * @param comment
     * @return
     */
    @Override
    public int saveComment(Comment comment) {
        // 通过动态的id查询动态信息,获取动态的作者id
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(comment.getPublishId()));
        Comment commentInDb = mongoTemplate.findOne(query, Comment.class);
        comment.setPublisherId(commentInDb.getUserId());// 评论的作者，谁发表的评论
        // 保存点赞信息
        mongoTemplate.save(comment);
        // 更新动态中的点赞数
        Update update = new Update();
        update.inc(comment.getCol(),1);
        mongoTemplate.updateFirst(query,update,Comment.class);
        // 返回最新的点赞数
        comment = mongoTemplate.findOne(query, Comment.class);
        // 默认使用点赞数量
        return comment.getLikeCount();
    }

    /**
     * 评论取消点赞
     * @param comment
     * @return
     */
    @Override
    public int removeComment(Comment comment) {
        Query query = new Query();
        // 构建删除的条件
        query.addCriteria(Criteria
                .where("userId").is(comment.getUserId())
                .and("commentType").is(comment.getCommentType())
                .and("publishId").is(comment.getPublishId())
        );
        mongoTemplate.remove(query,Comment.class);
        // 点赞数据要-1
        Update update = new Update();
        update.inc(comment.getCol(),-1);
        Query commentQuery = new Query();
        commentQuery.addCriteria(Criteria.where("_id").is(comment.getPublishId()));
        // 条件满足时只更新第一条
        mongoTemplate.updateFirst(commentQuery,update,Comment.class);

        // 获取最新的点赞数
        comment = mongoTemplate.findOne(commentQuery, Comment.class);
        // 获取最新的点赞数
        return comment.getLikeCount();
    }

    /**
     * 谁评论、点赞、喜欢了我
     * @param page
     * @param pagesize
     * @param commentType 1：点赞，2：评论，3：喜欢
     * @param userId 登陆用户的id, 条件，发布者的id
     * @return
     */
    @Override
    public PageResult findByUserId(Long page, Long pagesize, int commentType, Long userId) {
        // Comment
        Query query = new Query();
        query.addCriteria(Criteria.where("publisherId").is(userId).and("commentType").is(commentType));

        // 总数
        long total = mongoTemplate.count(query, Comment.class);
        PageResult pageResult = PageResult.total(total, page, pagesize);
        if(total > 0){
            query.with(PageRequest.of(page.intValue()-1,pagesize.intValue()))
                    .with(Sort.by(Sort.Order.desc("created")));
            List<Comment> commentList = mongoTemplate.find(query, Comment.class);
            pageResult.setItems(commentList);
        }
        return pageResult;
    }
}
