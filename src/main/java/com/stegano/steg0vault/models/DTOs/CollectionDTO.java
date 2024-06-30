package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class CollectionDTO {
    private String name;
    private String description;
    private String createdAt;
    private String modifiedAt;
    public boolean valid() {
        return name != null && description != null && !name.isEmpty();
    }
}
