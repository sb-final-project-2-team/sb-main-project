package com.codeit.closet.module.auth.dto;

import java.util.UUID;

public record AuthorDTO (
    UUID userId,
    String name,
    String profileImageUrl
){

}
