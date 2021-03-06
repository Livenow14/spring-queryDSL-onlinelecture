package com.livenow.querydsl.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id @GeneratedValue
    @Column(name =  "member_id")
    private Long id;

    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder
    public Member(String username, int age, Team team){
        this.username = username;
        this.age = age;

        if(team!=null){
            changTeam(team);
        }
    }


    public Member(String username, int age){
        this.username = username;
        this.age = age;

    }
    public void changTeam(Team team){
        this.team = team;
        team.getMembers().add(this);
    }
}
