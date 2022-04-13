package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  评论，点赞，喜欢的业务
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/11
 */
@Service
public class CommentService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Reference
    private CommentApi commentApi;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 动态点赞
     * @param publishId 动态的id
     * @return
     */
    public Integer like(String publishId) {
        // 获取登陆用户的id
        Long userId = UserHolder.getUserId();
        // 构建Comment对象添加点赞信息
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));
        comment.setCommentType(1);// 点赞
        comment.setPubType(1);// 动态   对动态点赞
        comment.setUserId(userId);
        comment.setCreated(System.currentTimeMillis());
        // 调用api完成点赞
        int count = commentApi.save(comment);
        // redis中标记当前用户对这个动态点赞过了
        String key = "publish_like_" + userId+"_" + publishId;
        redisTemplate.opsForValue().set(key,1);
        //返回最新点赞数
        return count;
    }

    /**
     * 取消点赞
     * @param publishId
     * @return
     */
    public Integer dislike(String publishId) {
        // 获取登陆用户的id
        Long userId = UserHolder.getUserId();
        // 构建Comment对象取消点赞
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));
        comment.setCommentType(1);// 点赞
        comment.setUserId(userId);
        // 调用api完成取消点赞
        int count = commentApi.remove(comment);
        // 取消redis中标记当前用户对这个动态点赞
        String key = "publish_like_" + userId+"_" + publishId;
        redisTemplate.delete(key);
        //返回最新点赞数
        return count;
    }

    /**
     * 动态喜欢
     * @param publishId 动态的id
     * @return
     */
    public Integer love(String publishId) {
        // 获取登陆用户的id
        Long userId = UserHolder.getUserId();
        // 构建Comment对象添加喜欢信息
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));
        comment.setCommentType(3);// 喜欢
        comment.setPubType(1);// 动态   对动态喜欢
        comment.setUserId(userId);
        comment.setCreated(System.currentTimeMillis());
        // 调用api完成喜欢
        int count = commentApi.save(comment);
        // redis中标记当前用户对这个动态喜欢过了
        String key = "publish_love_" + userId+"_" + publishId;
        redisTemplate.opsForValue().set(key,1);
        //返回最新喜欢数
        return count;
    }

    /**
     * 取消喜欢
     * @param publishId
     * @return
     */
    public Integer unlove(String publishId) {
        // 获取登陆用户的id
        Long userId = UserHolder.getUserId();
        // 构建Comment对象取消喜欢
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));
        comment.setCommentType(3);// 喜欢
        comment.setUserId(userId);
        // 调用api完成取消喜欢
        int count = commentApi.remove(comment);
        // 取消redis中标记当前用户对这个动态喜欢
        String key = "publish_love_" + userId+"_" + publishId;
        redisTemplate.delete(key);
        //返回最新点喜欢
        return count;
    }

    /**
     * 动态的评论列表
     * @param page
     * @param pagesize
     * @param publishId
     * @return
     */
    public PageResult findPage(Long page, Long pagesize, String publishId) {
        //1. 调用api查询评论分页结果
        PageResult pageResult = commentApi.findPage(page,pagesize,publishId);
        //2. 获取分页的结果集
        List<Comment> commentList = pageResult.getItems();
        if(CollectionUtils.isNotEmpty(commentList)) {
            //3. 遍历它，创建新list<ComementVo> list
            List<CommentVo> voList = new ArrayList<CommentVo>();
            for (Comment comment : commentList) {
                //4. 获取用户id, 查询用户信息
                // 评论者的用户id
                Long userId = comment.getUserId();
                UserInfo userInfo = userInfoApi.findById(userId);
                //5. 转成vo，
                CommentVo vo = new CommentVo();
                BeanUtils.copyProperties(userInfo, vo);
                BeanUtils.copyProperties(comment,vo);
                vo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));
                vo.setId(comment.getId().toHexString());
                // 判断登陆用户是否点赞过
                String key = "comment_like_" + UserHolder.getUserId() + "_" + vo.getId();
                if(redisTemplate.hasKey(key)){
                    vo.setHasLiked(1); // 点赞过了
                }
                // 添加list里
                voList.add(vo);
            }
            //6. 设置分页结果集
            pageResult.setItems(voList);
        }
        //7. 返回
        return pageResult;
    }

    /**
     * 动态发表评论
     * @param paramMap movementId, comment
     * @return
     */
    public int add(Map<String, String> paramMap) {
        // 评论的内容
        String content = paramMap.get("comment");
        // 动态的id
        String publishId = paramMap.get("movementId");
        // 构建comment
        Comment comment = new Comment();
        comment.setCreated(System.currentTimeMillis());
        comment.setCommentType(2);// 评论
        comment.setContent(content);
        comment.setUserId(UserHolder.getUserId());
        comment.setPublishId(new ObjectId(publishId));
        comment.setPubType(1);
        comment.setLikeCount(0); // 评论的点赞默认为0

        // 调用api添加, 获取评论数
        int count = commentApi.save(comment);
        return count;
    }

    /**
     * 评论的点赞
     * @param commentId
     * @return
     */
    public Integer likeComment(String commentId) {
        // 获取登陆用户的id
        Long userId = UserHolder.getUserId();
        // 构建Comment对象添加点赞信息
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(commentId));
        comment.setCommentType(1);// 点赞
        comment.setPubType(3);// 对评论 点赞
        comment.setUserId(userId);
        comment.setCreated(System.currentTimeMillis());
        // 调用api完成点赞
        int count = commentApi.saveComment(comment);
        // redis中标记当前用户对这个动态点赞过了
        String key = "comment_like_" + userId+"_" + commentId;
        redisTemplate.opsForValue().set(key,1);
        //返回最新点赞数
        return count;
    }

    /**
     * 评论取消点赞
     * @param commentId
     * @return
     */
    public Integer dislikeComment(String commentId) {
        // 获取登陆用户的id
        Long userId = UserHolder.getUserId();
        // 构建Comment对象取消点赞
        Comment comment = new Comment();
        // 操作哪张表的主键, 操作的是评论
        comment.setPublishId(new ObjectId(commentId));
        comment.setCommentType(1);// 点赞
        comment.setUserId(userId);
        // 调用api完成取消点赞
        int count = commentApi.removeComment(comment);
        // 取消redis中标记当前用户对这个动态点赞
        String key = "comment_like_" + userId+"_" + commentId;
        redisTemplate.delete(key);
        //返回最新点赞数
        return count;
    }
}
