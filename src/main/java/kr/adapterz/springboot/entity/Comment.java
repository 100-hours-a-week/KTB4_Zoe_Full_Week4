package kr.adapterz.springboot.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Comment {

    private Long id;
    private String content;
    private Post post;
    private User author;
    private LocalDateTime createdAt;

    public Comment(String content, Post post, User author) {
        this.content = content;
        this.post = post;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void assignId(Long id) {
        this.id = id;
    }
}
