package kr.adapterz.springboot.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PostListResponseDto {

    private List<PostSummaryResponseDto> posts;

    public PostListResponseDto(List<PostSummaryResponseDto> posts) {
        this.posts = posts;
    }
}
