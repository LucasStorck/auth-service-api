package com.lucas.JavaAuthenticator.dtos;

public record LoginResponseDto (String accessToken, Long expiresIn) {
}
