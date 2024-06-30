package com.stegano.steg0vault.models.DTOs;

import lombok.*;

@Data
@Builder
public class UserDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String createdAt;
    private String modifiedAt;
    private String role;
}
