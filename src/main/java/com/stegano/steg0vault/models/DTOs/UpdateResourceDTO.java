package com.stegano.steg0vault.models.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateResourceDTO {

    private String name;
    private String description;
    private String algorithm;
    private String newSecret;
}
