package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Action {
    private String name;
    private String info;
}
