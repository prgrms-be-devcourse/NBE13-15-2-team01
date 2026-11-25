package com.team01.ticketing.member.dto;

import com.team01.ticketing.member.entity.Member;
import com.team01.ticketing.member.entity.Role;

public record MemberResponse(Long id, String email, String name, Role role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName(), member.getRole());
    }
}
