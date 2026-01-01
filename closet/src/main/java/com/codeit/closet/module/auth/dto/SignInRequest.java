package com.codeit.closet.module.auth.dto;

public record SignInRequest(
    String username,
    String password
) {

}
