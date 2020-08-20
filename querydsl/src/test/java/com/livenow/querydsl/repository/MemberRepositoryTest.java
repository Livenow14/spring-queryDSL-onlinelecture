package com.livenow.querydsl.repository;

import com.livenow.querydsl.controller.MemberSearchCondition;
import com.livenow.querydsl.domain.Member;
import com.livenow.querydsl.domain.Team;
import com.livenow.querydsl.dto.MemberTeamDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(member).isEqualTo(findMember);

        List<Member> result = memberRepository.findAll();
        assertThat(result).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void QuerdslSearchTest() {
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();

        Member member1 = Member.builder().username("member1").age(10).team(teamA).build();
        Member member2 = Member.builder().username("member2").age(20).team(teamA).build();
        Member member3 = Member.builder().username("member3").age(30).team(teamB).build();
        Member member4 = Member.builder().username("member4").age(40).team(teamB).build();


        em.persist(teamA);
        em.persist(teamB);


        em.flush();
        em.clear();

        MemberSearchCondition condition = new MemberSearchCondition();
        /**
         * 실무에서 많이 하는 실수, 조건이 다 빠졌을 때
         * 이럴 땐, 쿼리문을 보면 다 불러온다. 이는 주니어가 하는 실수다.
         * 리미트를 주거나, 기본 조건을 두는것이 좋다.
         */
        condition.setAgeGoe(35);
        condition.setAgeLoe(45);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberRepository.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");

    }
}
