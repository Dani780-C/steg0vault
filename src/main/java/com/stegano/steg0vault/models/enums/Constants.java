package com.stegano.steg0vault.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Constants {
    SFTP_SERVER_REMOTE_DIRECTORY_NAME("upload"),
    DEFAULT_COLLECTION_NAME("Steg0Vault"),
    DEFAULT_COLLECTION_DESCRIPTION("Default collection"),
    DEFAULT_RESOURCE_NAME("Steg0Vault Password"),
    DEFAULT_RESOURCE_DESCRIPTION("File that embeds the password of the Steg0Vault application"),
    LOCAL_DIRECTORY_NAME("currentUserResources"),
    HEAD_FILE_NAME("StillCover");
    private final String value;
}
