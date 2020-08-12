package com.livenow.querydsl.dto;

import com.livenow.querydsl.domain.Member;
import lombok.Data;

@Data
public class UserDto {

    private String name;
    private int age;

/*    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }*/

    public UserDto(Member member) {
        this.name = member.getUsername();
        this.age = member.getAge();
    }
}
