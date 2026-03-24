package com.gridinsight.backend.a_iam.dto;

public record RefreshResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
}

