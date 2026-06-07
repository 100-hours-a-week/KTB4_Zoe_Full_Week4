package kr.adapterz.springboot.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Post {

    private Long id;

    private String title;
    private String content;

    private User author;

    private long viewCount;
    private LocalDateTime createdAt;

    public Post(String title, String content, User author) {
        this.title =  title;
        this.content = content;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

    public void  changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    //게시글 생성시 id 부여 메서드
    public void assignId(Long id) {
        this.id = id;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
