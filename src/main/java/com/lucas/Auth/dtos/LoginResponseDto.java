package com.lucas.Auth.dtos;

public record LoginResponseDto (String accessToken, String refreshToken, Long expiresIn) {
}
