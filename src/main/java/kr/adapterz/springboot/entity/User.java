package kr.adapterz.springboot.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
public class User {

    private Long id;

    private  String email;
    private  String password;
    private  String nickname;

    private  final List<Post> posts = new ArrayList<>();

    public User(Long id, String email, String password, String nickname) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}
