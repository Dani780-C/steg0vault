package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class PostResourceDTO {
    private ResourceDTO resourceDTO;
    private CollectionDTO collectionDTO;
    private String secretToEmbed;
}
