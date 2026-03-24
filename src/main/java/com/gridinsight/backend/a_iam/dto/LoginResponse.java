package com.gridinsight.backend.a_iam.dto;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
}
