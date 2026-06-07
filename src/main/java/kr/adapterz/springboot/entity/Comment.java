package kr.adapterz.springboot.entity;

import kr.adapterz.springboot.entity.Post;
import kr.adapterz.springboot.entity.User;
import lombok.Getter;

@Getter
public class Comment {

    private Long id;
    private String content;
    private Post post;
    private User author;

    public Comment(String content, Post post, User author) {
        this.content = content;
        this.post = post;
        this.author = author;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void assignId(Long id) {
        this.id = id;
    }
}