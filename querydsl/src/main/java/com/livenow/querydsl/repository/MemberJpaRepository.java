package com.livenow.querydsl.repository;

import com.livenow.querydsl.domain.Member;
import com.livenow.querydsl.domain.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

import static com.livenow.querydsl.domain.QMember.*;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    /**
     * 반울 주입하든, 이렇게 초기화하든 상관없다.
     * 빈을 주입해서 사용한다면 @RequiredArgsConstructor를 사용할 수 있다는 것이다.
     * 다만 빈을 사용한다면 테스트할때 따로 또 주입을 해줘야한다는 단점이있다.
     *
     * 밑에와 같이 쓰면 테스트 작성시 별다른 설정이 없어도 된다.
     */
    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    /**
     * 원래는 findAll로 해줘야함
     * 배우는 단계라 언더바를 넣음
     */
    public List<Member> findAll_Querydsl(){
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username){
        return em.createQuery("select m from Member m" +
                " where m.username =: username", Member.class)
                .setParameter("username",username )
                .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username){
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }


}
