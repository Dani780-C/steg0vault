package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@Builder
public class CollectionResourcesDTO {
    private CollectionDTO collectionDTO;
    private ArrayList<ResourceNameAndDescriptionDTO> resourceNameAndDescriptionDTO;
}
