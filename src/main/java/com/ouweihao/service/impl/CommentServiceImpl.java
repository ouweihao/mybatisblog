package com.ouweihao.service.impl;

import com.ouweihao.dao.BlogDao;
import com.ouweihao.dao.CommentDao;
import com.ouweihao.pojo.Comment;
import com.ouweihao.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private BlogDao blogDao;

    @Override
    public List<Comment> getCommentByBlogId(Long blogId) {  //查询父评论
        //没有父节点的默认为-1
        List<Comment> comments = commentDao.findByBlogIdAndParentCommentNull(blogId, Long.parseLong("-1"));
        return eachComment(comments);
    }

    @Override
    //接收回复的表单
    public int saveComment(Comment comment) {
        //获得父id
        Long parentCommentId = comment.getParentComment().getId();
        //没有父级评论默认是-1
        if (parentCommentId != -1) {
            //有父级评论
            comment.setParentComment(commentDao.findByParentCommentId(comment.getParentCommentId()));
//            commentDao.findByParentCommentId(comment.getParentCommentId()).getReplyComments().add(comment);
        } else {
            //没有父级评论
            comment.setParentCommentId((long) -1);
            comment.setParentComment(null);
        }
        comment.setCreateTime(new Date());
        return commentDao.saveComment(comment);
    }

    /**
     * 循环每个顶层评论
     * @param comments
     * @return 只有两层深度（顶层和下一层）的评论集合
     */
    private List<Comment> eachComment(List<Comment> comments) {
        List<Comment> commentsViews = new ArrayList<>();
        for (Comment comment : comments) {
            Comment c = new Comment();
            BeanUtils.copyProperties(comment, c);
            commentsViews.add(c);
        }
        // 合并评论中的各子层级到第一层级中
        combineChildren(commentsViews);
        return commentsViews;
    }

    /**
     * 修改顶层评论的reply集合
     * @param comments 顶层评论
     */
    private void combineChildren(List<Comment> comments) {
        for (Comment comment : comments) {
            List<Comment> replyComments = comment.getReplyComments();
            for (Comment replyComment : replyComments) {
                // 循环迭代，找出子代，存放到tempReplys中
                recursively(replyComment);
            }
            // 修改顶层评论的reply集合为迭代处理后的集合
            comment.setReplyComments(tempReplys);
            // 清空临时存放区
            tempReplys = new ArrayList<>();
        }
    }

    // 存放迭代找出的所有的子代的集合
    private List<Comment> tempReplys = new ArrayList<>();

    /**
     * 递归迭代查找回复评论
     * @param comment 顶级评论
     */
    private void recursively(Comment comment) {
        // 顶层节点添加到临时存放集合中
        tempReplys.add(comment);
        if (comment.getReplyComments().size() > 0) {
            List<Comment> replys = comment.getReplyComments();
            for (Comment reply : replys) {
                tempReplys.add(reply);
                // 递归查找回复评论
                if (reply.getReplyComments().size() > 0) {
                    recursively(reply);
                }
            }
        }
    }

}
