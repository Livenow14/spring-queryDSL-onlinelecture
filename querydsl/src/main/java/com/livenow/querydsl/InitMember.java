package com.livenow.querydsl;

import com.livenow.querydsl.domain.Member;
import com.livenow.querydsl.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * local에서만 돌게 해줌
 */
@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    /**
     * 자동적으로 실행하려고
     */
    @PostConstruct
    public void init() {
        initMemberService.init();;
    }

    @Component
    static class InitMemberService{
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init(){
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                new Member("member" + i, i, selectedTeam);
            }
            em.persist(teamA);
            em.persist(teamB);

        }
    }

}
