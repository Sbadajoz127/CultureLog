package com.cultureSL.CultureLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeResponse {
    private int likeCount;
    private boolean liked;
}
