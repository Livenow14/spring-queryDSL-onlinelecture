package com.livenow.querydsl.dto;

import com.livenow.querydsl.domain.Member;
import lombok.*;

@Getter
@NoArgsConstructor
@Setter
@ToString(of = {"username", "age"})
public class MemberDto {

    private String username;
    private int age;

    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }

}
