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

import static com.livenow.querydsl.domain.QMember.*;
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
    @Test
    public void Querydsltype(){

        /**
         *  static import로 QMember를 넣었다.
         *  웬만하면 이렇게 하자 !
         *
         *  같은 테이블을 조인하는 경우에는
         *  new QMember("member2"); 이런식으로 다시 만들어주자
         *  이런 경우는 거의 없다.
         */
        Member findMember = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))    // 파라미터 바인딩 없이 prepare statment로 파라미터 바인딩 해줌
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");

    }


    /**
     * jpql의 모든 검색조건 제공
     * member.username.eq("member1") // username = 'member1'
     * member.username.ne("member1") //username != 'member1'
     * member.username.eq("member1").not() // username != 'member1'
     * member.username.isNotNull() //이름이 is not null
     * member.age.in(10, 20) // age in (10,20)
     * member.age.notIn(10, 20) // age not in (10, 20)
     * member.age.between(10,30) //between 10, 30
     * member.age.goe(30) // age >= 30member.age.gt(30) // age > 30
     * member.age.loe(30) // age <= 30
     * member.age.lt(30) // age < 30
     * member.username.like("member%") //like 검색
     * member.username.contains("member") // like ‘%member%’ 검색
     * member.username.startsWith("member") //like ‘member%’ 검색
     */
    @DisplayName("검색조건 쿼리 ")
    @Test
    public void search() throws Exception{
        //given
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        //when

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * and의 경우 쉼표로 해결가능
     * null을 넣을 수 있기 때문에 동적쿼리에서 기가 막히다. 
     */
    @DisplayName("and 파라미터 ")
    @Test
    public void searchAndParam() throws Exception{
        //given
        Member findMember = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        ,member.age.eq(10))
                .fetchOne();
        //when

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

}
