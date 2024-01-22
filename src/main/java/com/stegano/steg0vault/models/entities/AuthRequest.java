package com.stegano.steg0vault.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.NonNull;

@Data
@Builder
@AllArgsConstructor
public class AuthRequest {
    @NonNull
    private String email;
    @NonNull
    private String password;

}
