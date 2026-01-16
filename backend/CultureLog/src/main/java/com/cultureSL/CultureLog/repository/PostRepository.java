package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.author.id = :userId OR p.author.id IN " +
           "(SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId AND f.status = 'ACCEPTED') " +
           "ORDER BY p.createdAt DESC")
    Page<Post> findNewsFeed(@Param("userId") Long userId, Pageable pageable);

    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}