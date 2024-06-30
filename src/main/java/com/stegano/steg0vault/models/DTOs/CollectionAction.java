package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CollectionAction {
    private String name;
    private List<Action> actionList;
}
