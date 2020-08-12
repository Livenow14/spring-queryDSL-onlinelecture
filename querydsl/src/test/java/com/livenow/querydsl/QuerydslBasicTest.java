package com.livenow.querydsl;


import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.livenow.querydsl.domain.Member;
import com.livenow.querydsl.domain.QMember;
import com.livenow.querydsl.domain.Team;
import com.livenow.querydsl.dto.MemberDto;
import com.livenow.querydsl.dto.UserDto;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;
import java.util.stream.Collectors;

import static com.livenow.querydsl.domain.QMember.member;
import static com.livenow.querydsl.domain.QTeam.*;
import static com.querydsl.jpa.JPAExpressions.*;
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

    /**
     * on절을 활용한 조인
     * 1. 조인 대상 필터링
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA;
     */
    @DisplayName("on절 필터링 ")
    @Test
    public void join_on_filtering() throws Exception{
        //given
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        //when
        for (Tuple tuple : result) {
            System.out.println("tuple1 = " + tuple);
        }

        /**
         * 외부조인은 대상이 아닌 것도 가져오지만
         * 그냥 join을 하게되면 대상이 내부이기 때문에 teamA인것만 가져옴
         *  on 절을 활용해 조인 대상을 필터링 할 때, 외부조인이 아니라 내부조인(inner join)을 사용하면,
         * where 절에서 필터링 하는 것과 기능이 동일하다. 따라서 on 절을 활용한 조인 대상 필터링을 사용할 때,
         * 내부조인 이면 익숙한 where 절로 해결하고, 정말 외부조인이 필요한 경우에만 이 기능을 사용하자.
         */
        //given
        List<Tuple> result2 = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        List<Tuple> result3 = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        //when
        for (Tuple tuple : result2) {
            System.out.println("tuple2 = " + tuple);
        }
        for (Tuple tuple : result3) {
            System.out.println("tuple3 = " + tuple);
        }
    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     *
     */
    @Test
    public void join_on_no_relation() throws Exception{
        //given
        em.persist(Member.builder().username("teamA").build());
        em.persist(Member.builder().username("teamB").build());

        /**
         * 막 조인을 할것이라서 leftJoni에서 그냥 team 이 들어감감
         * 다 나오지만 버의 이름과 팀의 이름이 같은경우만 조인되서 팀을 가져옴
         *
         * 하이버네이트 5.1부터 on 을 사용해서 서로 관계가 없는 필드로 외부 조인하는 기능이 추가되었다. 물론 내
         * 부 조인도 가능하다.
         * 주의! 문법을 잘 봐야 한다. leftJoin() 부분에 일반 조인과 다르게 엔티티 하나만 들어간다.
         * 일반조인: leftJoin(member.team, team)
         * on조인: from(member).leftJoin(team).on(xxx)
        */

        //when
        List<Tuple> fetch = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        //then
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }

        List<Member> fetch1 = queryFactory
                .select(member)
                .from(member)
                .join(team).on(member.username.eq(team.name))
                .fetch();
        for (Member member1 : fetch1) {
            System.out.println("member1 = " + member1.getUsername());
        }
    }

    /**
     * 패치 조인
     * 페치 조인은 SQL에서 제공하는 기능은 아니다. SQL조인을 활용해서 연관된 엔티티를 SQL 한번에 조회하
     * 는 기능이다. 주로 성능 최적화에 사용하는 방법이다.
     */

    @PersistenceUnit
    EntityManagerFactory emf;

    @DisplayName("패치 조인 없을때")
    @Test
    public void NoFetchJoin() throws Exception{
        //given
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        //when
        //로딩된 데이터인지, 아직 초기화가 안된 데이터인지 알려줌
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치조인 미적용").isTrue();
    }
    /**
     * 서브쿼리
     * com.querydsl.jpa.JPAExpressions 시용
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")    //나이 필드를 꺼냄
                .containsExactly(40);
    }

     /**
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    public void subQueryAvg() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.gt(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")    //나이 필드를 꺼냄
                .containsExactly(30, 40);
    }

    /**
     * In 쿼리
     * In이 처리되는걸 본다.
     * 억지성 예제이다.
     */
    @Test
    public void subQueryIn() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")    //나이 필드를 꺼냄
                .containsExactly(20, 30, 40);
    }

    /**
     * select 서브쿼리
     * 멤버들의 이름과 평균 나이를 보여주자
     * JPAExpressions는 static import가 가능함.
     */
    @Test
    public void subQuerySelect() throws Exception {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> fetch = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * from 절의 서브쿼리 한계
     * JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl
     * 도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도 하
     * 이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다
     *
     * from 절의 서브쿼리 해결방안
     * 1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
     * 2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
     * 3. nativeSQL을 사용한다.
     *
     * from 절에서 서브쿼리를 쓰는 많은 이유
     *  DB를 어떻게든 화면에 맞추려고 하니까 그렇다.
     *  sql을 잘못 쓰는 방향이다.
     *
     *  해결방안
     *  db는 데이터만 가져오는것
     *  그거에 대한 조작은 서비스단에서 하는 것이다
     *
     *  sql antiPatterns책을 보자
     */

    /**
     * Case 문
     */

    @Test
    public void basicCase(){
        List<String> reulst = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : reulst) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 복잡한 case
     * 왠만하면 db는 데이터를 필터링과 그룹화만 하고
     * 실제 전화하고 바꾸고 하는것은 db에서 하는 건 아니다.
     *
     * DB에선 필요한 기능이 아님.. 명심!
     */
    @Test
    public void complexCase(){
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21살~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

    }

    /**
     * 상수
     */

    @Test
    public void constant(){
        List<Tuple> a = queryFactory
                .select(member.username, Expressions.constant("A"))     //상수A를 그냥 보여줌
                .from(member)
                .fetch();
        for (Tuple tuple : a) {
            System.out.println("tuple = " + tuple);
        }

    }

    /**
     * 문자 더하기
     */
    @Test
    public void concat(){

        //{username}_{age}
        List<String> fetch = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))       //concat을 잘 씀
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 프로젝션
     * 프로젝션: select 대상 지정
     */

    /**
     * 프로젝션 대상이 하나
     */
    @Test
    public void simpleProjection() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }

        List<Member> result2 = queryFactory
                .select(member)
                .from(member)
                .fetch();
        for (Member member1 : result2) {
            System.out.println("member1 = " + member1);
        }
    }

    /**
     * 튜플 프로젝션
     * 이것은, 레포지토리 단에서 써야한다.
     * package com.querydsl.core;
     * 의 패키지 안에서 있기때문에
     *
     * 밖에서 던질 땐 DTO로 바꿔서 함
     *
     */
    @Test
    public void tupleProjection(){
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    /**
     * DTO로 조회
     * 순수 JPA에서 DTO 조회 코드
     */
    @Test
    public void findDtoByJPQL(){
        List<Member> findByJPQL = em.createQuery("select m from Member m",
                Member.class)
                .getResultList();
        List<MemberDto> collect = findByJPQL.stream()
                .map(m -> new MemberDto(m.getUsername(), m.getAge()))
                .collect(Collectors.toList());
        for (MemberDto memberDto : collect) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * Querydsl 빈 생성(Bean population)
     * 결과를 DTO 반환할 때 사용
     * 다음 3가지 방법 지원
     * - 프로퍼티 접근
     * - 필드 직접 접근
     * - 생성자 사용
     */

    /**
     * 프로퍼터 접근
     */
    @Test
    public void findDtoBySetter(){
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 필드접근(필드명이 맞아야함 )
     */
    @Test
    public void findDtoByField(){
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 필드접근(필드명이 다를때 )
     */
    @Test
    public void findUserDtoByField(){
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

    /**
     * 필드접근(서브 쿼리를 사용할때)
     * 이건 잘 상용하지 않음
     */
    @Test
    public void findUserDtoSubQueryByField(){
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub), "age")))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

    /**
     * 생성자 방식
     */
    @Test
    public void findDtoByConstructor(){
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    @Test
    public void findUserDtoByConstructor(){
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }

}
