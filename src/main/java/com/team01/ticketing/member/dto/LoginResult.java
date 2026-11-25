package com.team01.ticketing.member.dto;

// 로그인 결과 : accessToken과 회원 정보는 body로, refreshToken은 컨트롤러가 쿠키로 보냄.
public record LoginResult(String accessToken, String refreshToken, MemberResponse member) {

    // body에 담을 응답 DTO만 꺼냄.
    public LoginResponse toResponse() {
        return new LoginResponse(accessToken, member);
    }
}
