package com.livenow.querydsl.repository;

import com.livenow.querydsl.controller.MemberSearchCondition;
import com.livenow.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    /**
     * 스프링 데이터 페이징 활용
     * 스프링 데이터의 Page, Pageable을 활용해보자.
     * 1. 전체 카운트를 한번에 조회하는 단순한 방법
     * 2. 데이터 내용과 전체 카운트를 별도로 조회하는 방법
     */

    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);

}
