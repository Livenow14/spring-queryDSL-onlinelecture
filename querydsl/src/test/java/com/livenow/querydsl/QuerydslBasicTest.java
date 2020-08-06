package com.livenow.querydsl;


import com.livenow.querydsl.domain.Member;
import com.livenow.querydsl.domain.QMember;
import com.livenow.querydsl.domain.QTeam;
import com.livenow.querydsl.domain.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.livenow.querydsl.domain.QMember.member;
import static com.livenow.querydsl.domain.QTeam.*;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;


    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        //given
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        
        Member member1 = Member.builder().username("member1").age(10).team(teamA).build();
        Member member2 = Member.builder().username("member2").age(20).team(teamA).build();
        Member member3 = Member.builder().username("member3").age(30).team(teamB).build();
        Member member4 = Member.builder().username("member4").age(40).team(teamB).build();

/*        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);*/ //cascade options을 team에다 둠
        
        em.persist(teamA);
        em.persist(teamB);


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

        Member findMember = queryFactory
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
        Member findMember = queryFactory
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
     * member.age.goe(30) // age >= 30
     * member.age.gt(30) // age > 30
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
        Member findMember = queryFactory
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
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        ,member.age.eq(10))
                .fetchOne();
        //when

        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 결과 조회
     * fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
     * fetchOne() : 단 건 조회
     * 결과가 없으면 : null
     * 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
     * fetchFirst() : limit(1).fetchOne()
     *
     * fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행/ 복잡하고 성능이 중요할때는 이거 쓰면 안되고 쿼리문 두번 써야함
     * fetchCount() : count 쿼리로 변경해서 count 수 조회
     */

    @DisplayName("결과 조회 테스트")
    @Test
    public void resultFetch(){
/*
        //given
        List<Member> fetch = jpaQueryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = jpaQueryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchFirst = jpaQueryFactory
                .selectFrom(QMember.member)
                .fetchFirst();
*/

        /**
         * 페이징용 쿼리 
         */
        QueryResults<Member> memberQueryResults = queryFactory
                .selectFrom(member)
                .fetchResults();
        memberQueryResults.getTotal();
        memberQueryResults.getResults();

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();


        //when

        //then
    }


    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차손(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     *
     */
    @DisplayName("정렬 예제제")
    @Test
    public void sort() throws Exception{
        //given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        //when
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        //then
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();

    }

    @DisplayName("페이징")
    @Test
    public void paging1() throws Exception{
        //given
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        //when

        //then
        assertThat(result.size()).isEqualTo(2);
    }

    @DisplayName("페이징2")
    @Test
    public void paging2() throws Exception{
        //given
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        //then
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);

        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @DisplayName("집합")
    @Test
    public void aggregation() throws Exception{
        //given
        /**
         * queryDsl이 제공하는 tuple
         * 튜플은 많이 쓰진않고 dto를 통해서 해결함
         */
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        //when
        Tuple tuple = result.get(0);
        //then
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     *
     */
    @DisplayName("Group")
    @Test
    public void group() throws Exception{
        //given
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        //when
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @DisplayName("기본조인")
    @Test
    public void join() throws Exception{
        //given
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        //when
        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");


        List<Member> leftJoin = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(leftJoin)
                .extracting("username")
                .containsExactly("member1", "member2");


    }

    /**
     * 연관관계가 없는경우 세타조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     * 외부 조인 불가능 -> on절을 사용하면 가능
     * 성능 최적화는 알아서 해줌
     */
    @DisplayName("세타 조인")
    @Test
    public void theta_join() throws Exception{
        //given
        em.persist(Member.builder().username("teamA").build());
        em.persist(Member.builder().username("teamB").build());

        //when
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        //then
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");
    }

}
