package com.team01.ticketing.member.service;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import com.team01.ticketing.member.dto.MemberResponse;
import com.team01.ticketing.member.dto.MemberUpdateRequest;
import com.team01.ticketing.member.dto.SignupRequest;
import com.team01.ticketing.member.entity.Member;
import com.team01.ticketing.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 회원 자원(가입, 조회, 수정)을 담당.
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        // 1. 이미 가입된 이메일이면 409 에러
        if(memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }
        // 2. 평문 비밀번호를 해시로 바꾼 뒤 USER 권한의 새 회원 생성
        Member member = Member.create(request.email(), passwordEncoder.encode(request.password()), request.name());
        // 3. INSERT 후 id가 채워진 회원을 응답 DTO로 바꿈.
        try {
            return MemberResponse.from(memberRepository.save(member));

        } catch (DataIntegrityViolationException ex) {
            // 같은 이메일로 동시에 가입할 경우 1번 검사를 둘 다 통과할 수 있음.
            // 이때 DB의 UNIQUE 제약이 두 번째 저장을 막고, 그 오류를 500 대신 409로 바꿈.
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }
    }

    // 내 정보 조회 : 토큰에서 꺼낸 회원 id로 회원을 찾아 응답 DTO로 변환.
    @Transactional(readOnly = true)
    public MemberResponse getMe(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMe(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.changeName(request.name());
        return MemberResponse.from(member);
    }
}
