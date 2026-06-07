package kr.adapterz.springboot.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CommentListResponseDto {

    private List<CommentResponseDto> comments;
    private PaginationResponseDto pagination;

    public CommentListResponseDto(List<CommentResponseDto> comments, PaginationResponseDto pagination) {
        this.comments = comments;
        this.pagination = pagination;
    }
}
