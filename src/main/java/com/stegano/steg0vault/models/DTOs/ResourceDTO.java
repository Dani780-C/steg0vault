package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class ResourceDTO {
    private Long id;
    private String name;
    private String type;
    private String description;
    private String algorithm;
    private String imageBytes;
    private String createdAt;
    private String modifiedAt;
    public boolean valid() {
        return name != null && !name.isEmpty() &&
               type != null && !type.isEmpty() &&
               description != null &&
               algorithm != null && !algorithm.isEmpty() &&
               imageBytes != null && !imageBytes.isEmpty();
    }
}
