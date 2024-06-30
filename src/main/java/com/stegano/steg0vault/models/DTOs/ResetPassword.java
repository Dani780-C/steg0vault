package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
public class ResetPassword {
    private String newPassword;
    private String retypedNewPassword;
    private String token;
}
