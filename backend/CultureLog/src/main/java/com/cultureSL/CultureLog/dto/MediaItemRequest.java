package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.MediaStatus;
import com.cultureSL.CultureLog.model.enums.MediaType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MediaItemRequest {
    private String title;
    private MediaType type;
    private MediaStatus status;
    private String genre;
    private Integer rating;
    private String comment;
    private LocalDate releaseDate;
}