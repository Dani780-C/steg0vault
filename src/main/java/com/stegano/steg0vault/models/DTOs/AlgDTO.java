package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlgDTO {
    private Long position;
    private Long id;
    private String name;
    private String deletedAt;
    private String createdAt;
}
