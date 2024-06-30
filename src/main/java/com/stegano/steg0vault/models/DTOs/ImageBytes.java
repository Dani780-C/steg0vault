package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class ImageBytes {
    private String imageBytes;
}
