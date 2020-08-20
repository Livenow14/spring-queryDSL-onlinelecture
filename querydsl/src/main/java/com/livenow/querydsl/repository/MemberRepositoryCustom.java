package com.livenow.querydsl.repository;

import com.livenow.querydsl.controller.MemberSearchCondition;
import com.livenow.querydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
