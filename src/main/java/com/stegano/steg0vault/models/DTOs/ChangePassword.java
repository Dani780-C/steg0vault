package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Builder
public class ChangePassword {
    private String newPassword;
    private String retypedNewPassword;
}
