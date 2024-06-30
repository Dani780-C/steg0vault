package com.stegano.steg0vault.models.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPassword {
    private String email;
}
