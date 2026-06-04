package kr.adapterz.springboot.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class User {

    private Long id;

    private  String email;
    private  String password;
    private  String nickname;

    List<Post> posts = new ArrayList<>();

    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    //유저 생성시 id 부여 메서드
    public void assignId(Long id) {
        this.id = id;
    }
}
