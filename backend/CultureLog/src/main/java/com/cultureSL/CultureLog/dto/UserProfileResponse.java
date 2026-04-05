package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String profilePictureUrl;
    private String profilePrivacy;
    private int postCount;
    private int followerCount;
    private int followingCount;
    private String followStatus;
    private boolean ownProfile;
    private boolean showFutureList;

    private List<PostResponse> posts;
    private List<MediaItemResponse> libraryItems;
}
