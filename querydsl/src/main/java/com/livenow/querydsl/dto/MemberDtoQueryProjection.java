package com.livenow.querydsl.dto;

import com.livenow.querydsl.domain.Member;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Getter
@NoArgsConstructor
@Setter
@ToString(of = {"username", "age"})
public class MemberDtoQueryProjection {

    private String username;
    private int age;

    @QueryProjection
    public MemberDtoQueryProjection(String username, int age) {
        this.username = username;
        this.age = age;
    }

}
