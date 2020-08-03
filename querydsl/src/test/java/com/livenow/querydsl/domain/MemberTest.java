package com.livenow.querydsl.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;


import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired
    EntityManager em;

    @DisplayName("엔티티 테스트")
    @Test
    public void entityTest() throws Exception{
        //given
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();

/*        em.persist(teamA);
        em.persist(teamB);*/ //cascadeTyep.All을 member에 두엇기 때문에 안해도 됨

        Member mamber1 = Member.builder().username("mamber1").age(10).team(teamA).build();
        Member mamber2 = Member.builder().username("mamber2").age(20).team(teamA).build();
        Member mamber3 = Member.builder().username("mamber3").age(30).team(teamB).build();
        Member mamber4 = Member.builder().username("mamber4").age(40).team(teamB).build();

        em.persist(mamber1);
        em.persist(mamber2);
        em.persist(mamber3);
        em.persist(mamber4);

        em.flush();
        em.clear();
        //when

        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        Assertions.assertThat(members.get(0).getAge()).isEqualTo(10);
        Assertions.assertThat(members.get(0).getTeam().getName()).isEqualTo("teamA");
        Assertions.assertThat(members.get(2).getAge()).isEqualTo(30);

        //then
    }

}