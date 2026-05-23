package com.cultureSL.CultureLog.dto;

import com.cultureSL.CultureLog.model.enums.TagColor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {

    @NotBlank(message = "El nombre de la etiqueta es obligatorio")
    private String name;

    private TagColor color = TagColor.POR_DEFECTO;
}
