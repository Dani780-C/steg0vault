package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@Data
public class Details {
    private long numberOfCollections;
    private long numberOfResources;
    private long totalMemory;
    private List<Action> userLogs;
    private List<CollectionAction> collectionLogs;
    private List<CollectionAction> resourceLogs;
}
