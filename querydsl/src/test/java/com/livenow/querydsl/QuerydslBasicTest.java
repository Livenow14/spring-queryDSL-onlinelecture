package com.livenow.querydsl;


import com.livenow.querydsl.domain.Member;
import com.livenow.querydsl.domain.QMember;
import com.livenow.querydsl.domain.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;


    @BeforeEach
    public void before(){
        jpaQueryFactory = new JPAQueryFactory(em);
        //given
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();

/*        em.persist(teamA);
        em.persist(teamB);*/ //cascadeTyep.All을 member에 두엇기 때문에 안해도 됨

        Member member1 = Member.builder().username("member1").age(10).team(teamA).build();
        Member member2 = Member.builder().username("member2").age(10).team(teamA).build();
        Member member3 = Member.builder().username("member3").age(10).team(teamB).build();
        Member member4 = Member.builder().username("member4").age(10).team(teamB).build();

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);


        em.flush();
        em.clear();
    }

    @DisplayName("jpql & querydsl 비교")
    @Test
    public void startJPQL () throws Exception{
        //given
        String qlString=
                "select m from Member m "+
                "where m.username =: username";

        /**
         * jpql은 밑의 에러를 실제 메서드를 사용할 때(런타임 때) 내줌.( 에러를 찾기 힘들다 )
         */

/*        String qlString=
                "select m from Member m "+
                        "where m.usernameee =: username";*/

        Member findByJPQL = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        //when
        assertThat(findByJPQL.getUsername()).isEqualTo("member1");


        //then
    }

    @Test
    public void startQuerydsl(){

        QMember m = new QMember("m");

        Member findMember = jpaQueryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))    // 파라미터 바인딩 없이 prepare statment로 파라미터 바인딩 해줌
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");

        /**
         * querydsl은 이런 에러를 컴파일 타임에 알려줌, -> 헨들링이 쉽다.
         */
/*        Member findMember = jpaQueryFactory
                .select(m)
                .from(m)
                .where(m.usernameee.eq("member1"))    // 파라미터 바인딩 없이 prepare statment로 파라미터 바인딩 해줌
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");*/

    }

}
