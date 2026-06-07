package kr.adapterz.springboot.entity;

import lombok.Getter;

@Getter
public class Like {

    private Long id;
    private Post post;
    private User user;

    public Like(Post post, User user) {
        this.post = post;
        this.user = user;
    }

    public void assignId(Long id) {
        this.id = id;
    }
}