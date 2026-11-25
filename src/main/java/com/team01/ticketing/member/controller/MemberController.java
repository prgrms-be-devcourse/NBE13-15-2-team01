package com.team01.ticketing.member.controller;

import com.team01.ticketing.member.dto.MemberResponse;
import com.team01.ticketing.member.dto.SignupRequest;
import com.team01.ticketing.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입")
    public MemberResponse signup(@Valid @RequestBody SignupRequest request) {

        return memberService.signup(request);
    }

}
