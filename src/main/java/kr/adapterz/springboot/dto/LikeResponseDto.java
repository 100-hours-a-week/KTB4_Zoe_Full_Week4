package kr.adapterz.springboot.dto;

import kr.adapterz.springboot.repository.LikeRepository;
import lombok.Getter;

@Getter
public class LikeResponseDto {

    private long likeCount;
    private  boolean liked;

    public LikeResponseDto(long likeCount, boolean liked) {
        this.likeCount = likeCount;
        this.liked = liked;
    }
}
