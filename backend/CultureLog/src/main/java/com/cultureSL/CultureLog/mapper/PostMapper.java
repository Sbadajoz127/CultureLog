package com.cultureSL.CultureLog.mapper;

import com.cultureSL.CultureLog.dto.CommentResponse;
import com.cultureSL.CultureLog.dto.PostResponse;
import com.cultureSL.CultureLog.model.Post;
import com.cultureSL.CultureLog.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final PostLikeRepository postLikeRepository;

    public PostResponse toDto(Post post, Long currentUserId) {
        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getUsername())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .likedByCurrentUser(isLiked)
                .linkedItemId(post.getLinkedItem() != null ? post.getLinkedItem().getId() : null)
                .linkedItemTitle(post.getLinkedItem() != null ? post.getLinkedItem().getTitle() : null)
                .linkedItemType(post.getLinkedItem() != null ? post.getLinkedItem().getType().name() : null)
                .linkedItemRating(post.getLinkedItem() != null ? post.getLinkedItem().getRating() : null)
                .recentComments(post.getComments().stream()
                        .limit(3)
                        .map(c -> new CommentResponse(
                                c.getId(),
                                c.getText(),
                                c.getAuthor().getUsername(),
                                c.getAuthor().getId(),
                                c.getCreatedAt()))
                        .collect(Collectors.toList()))
                .build();
    }
}