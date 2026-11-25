package com.team01.ticketing.member.dto;

// 재발급 결과 : accessToken은 body(AccessTokenResponse)로, refreshToken은 쿠키로 보냄.
public record IssuedTokens(String accessToken, String refreshToken) {

    public AccessTokenResponse toResponse() {
        return new AccessTokenResponse(accessToken);
    }
}
