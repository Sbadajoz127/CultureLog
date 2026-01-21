package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.MediaItem;
import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {

    List<MediaItem> findByUserId(Long userId);

    List<MediaItem> findByUserIdAndType(Long userId, MediaType type);

    List<MediaItem> findByUserIdAndStatus(Long userId, MediaStatus status);
    
    List<MediaItem> findByUserIdAndGenre(Long userId, String genre);

    List<MediaItem> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
    
    List<MediaItem> findByUserIdAndTags_Name(Long userId, String tagName);
}