package com.stegano.steg0vault.models.DTOs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoDTO {
    private Long position;
    private Long id;
    private String fullName;
    private String email;
    private String createdAt;
    private String deletedAt;
    private String role;
    private String lastActiveDate;
}
