package com.team01.ticketing.member;

public record LoginResponse(String accessToken, MemberResponse member) {
}
