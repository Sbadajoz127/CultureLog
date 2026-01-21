package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.model.Comment;
import com.cultureSL.CultureLog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    
    Post createPost(Long userId, String content, Long linkedMediaItemId);

    Page<Post> getNewsFeed(Long userId, Pageable pageable);

    Page<Post> getPostsByUserId(Long userId, Pageable pageable);

    void toggleLike(Long postId, Long userId);

    Comment addComment(Long postId, Long userId, String text);

    List<Comment> getCommentsForPost(Long postId);
}