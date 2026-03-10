package com.gridinsight.backend.IAM_1.dto;

public record RefreshResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
}

