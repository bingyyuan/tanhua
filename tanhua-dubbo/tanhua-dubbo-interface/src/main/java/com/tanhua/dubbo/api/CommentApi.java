package com.tanhua.dubbo.api;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

/**
 * <p>
 *
 * </p>
 *
 * @author: Eric
 * @since: 2021/3/11
 */
public interface CommentApi {
    /**
     * 动态点赞
     * @param comment
     * @return 最新点赞数
     */
    int save(Comment comment);

    /**
     * 取消点赞
     * @param comment
     * @return
     */
    int remove(Comment comment);

    /**
     * 动态的评论列表
     * @param page
     * @param pagesize
     * @param publishId
     * @return
     */
    PageResult findPage(Long page, Long pagesize, String publishId);

    /**
     * 对评论点赞
     * @param comment
     * @return
     */
    int saveComment(Comment comment);

    /**
     * 评论取消点赞
     * @param comment
     * @return
     */
    int removeComment(Comment comment);


    /**
     * 谁评论、点赞、喜欢了我
     * @param page
     * @param pagesize
     * @param commentType 1：点赞，2：评论，3：喜欢
     * @param userId 登陆用户的id, 条件，发布者的id
     * @return
     */
    PageResult findByUserId(Long page, Long pagesize, int commentType, Long userId);
}
