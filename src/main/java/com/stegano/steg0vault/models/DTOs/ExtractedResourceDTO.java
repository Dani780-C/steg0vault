package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class ExtractedResourceDTO {
    private String name;
    private String algorithm;
    private String imageBytes;
    private String message;
}
