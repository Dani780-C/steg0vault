package com.stegano.steg0vault.models.DTOs;

import com.stegano.steg0vault.models.enums.AlgorithmType;
import com.stegano.steg0vault.models.enums.ImageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
public class ResourceDTO {
    private String name;
    private String type;
    private String description;
    private String algorithm;
    private boolean isSaved;
    private String imageBytes;
    public boolean valid() {
        return name != null && !name.isEmpty() &&
               type != null && !type.isEmpty() && ImageType.valid(type) &&
               description != null &&
               algorithm != null && !algorithm.isEmpty() && AlgorithmType.valid(algorithm) &&
               imageBytes != null && !imageBytes.isEmpty();
    }
}
