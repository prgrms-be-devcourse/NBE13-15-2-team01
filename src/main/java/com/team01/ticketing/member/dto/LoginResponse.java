package com.team01.ticketing.member.dto;

public record LoginResponse(String accessToken, MemberResponse member) {
}
