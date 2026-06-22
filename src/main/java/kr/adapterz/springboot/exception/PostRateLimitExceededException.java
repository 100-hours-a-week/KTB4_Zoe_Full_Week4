package kr.adapterz.springboot.exception;

public class PostRateLimitExceededException extends RuntimeException {

    public PostRateLimitExceededException() {
        super("짧은 시간 안에 게시글을 너무 많이 작성했습니다.");
    }
}
