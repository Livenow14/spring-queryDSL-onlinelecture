package com.livenow.querydsl.controller;

import com.livenow.querydsl.dto.MemberTeamDto;
import com.livenow.querydsl.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    /**
     * http://localhost:8080/v1/member?teamName=teamB&ageGoe=31&ageLoe=35
     * 이런식으로 조회 가능하다
     */
    @GetMapping("/v1/member")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }
}
