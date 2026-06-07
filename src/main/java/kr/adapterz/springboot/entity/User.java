package kr.adapterz.springboot.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class User {

    private Long id;

    private  String email;
    private  String password;
    private  String nickname;
    private String profileImage;

    private  final List<Post> posts = new ArrayList<>();

    public User(Long id, String email, String password, String nickname, String profileImage) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}
