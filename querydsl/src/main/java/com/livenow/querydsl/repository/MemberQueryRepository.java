package com.livenow.querydsl.repository;

import com.livenow.querydsl.controller.MemberSearchCondition;
import com.livenow.querydsl.dto.MemberTeamDto;
import com.livenow.querydsl.dto.QMemberTeamDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.livenow.querydsl.domain.QMember.member;
import static com.livenow.querydsl.domain.QTeam.team;

/**
 * 조회, 즉 쿼리에 너무 특화되있으면 이렇게 분리를 해서 하는게 좋음
 * 너무 Custom에 때려 박는 것도 안좋은 방법이다
 *
 * 중요한 것은 핵심 비스니스 로직으로 재사용 가능성이 있는 것들은
 * MemberRepository에 넣고, 공용성이 없고 특정 api에 종속 되어잇다면
 * 이렇게 별도로 조회용 리포지토리를 만드는게 좋음
 */
@Repository
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MemberQueryRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<MemberTeamDto> search(MemberSearchCondition condition){
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    /**
     * queryDsl 의 BooleanExpression이니 헷갈리지 말기
     */
    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName): null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }


    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
