package com.lucas.Auth.dtos;

public record LoginResponseDto (String accessToken, Long expiresIn) {
}
