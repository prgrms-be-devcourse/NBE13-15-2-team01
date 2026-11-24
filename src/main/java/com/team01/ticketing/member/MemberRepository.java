package com.team01.ticketing.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
    //회원 가입 시 중복 이메일 확인
    boolean existsByEmail(String email);
}
