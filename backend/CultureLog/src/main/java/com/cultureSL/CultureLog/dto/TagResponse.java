package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.TagColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String name;
    private TagColor color;
    private String colorHex;
}
