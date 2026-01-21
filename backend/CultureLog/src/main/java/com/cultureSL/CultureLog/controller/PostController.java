package com.cultureSL.CultureLog.controller;

import com.cultureSL.CultureLog.dto.CommentRequest;
import com.cultureSL.CultureLog.dto.PostRequest;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.mapper.PostMapper;
import com.cultureSL.CultureLog.model.Post;
import com.cultureSL.CultureLog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestParam Long userId,
            @RequestBody PostRequest request) {
        
        Post createdPost = postService.createPost(userId, request.getContent(), request.getLinkedMediaItemId());
        return ResponseEntity.ok(postMapper.toDto(createdPost, userId));
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postService.getNewsFeed(userId, pageable);

        Page<PostResponse> dtoPage = postPage.map(post -> postMapper.toDto(post, userId));

        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long postId, @RequestParam Long userId) {
        postService.toggleLike(postId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Void> addComment(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @RequestBody CommentRequest request) {
        
        postService.addComment(postId, userId, request.getText());
        return ResponseEntity.ok().build();
    }
}