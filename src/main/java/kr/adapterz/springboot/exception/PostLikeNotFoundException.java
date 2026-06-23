package kr.adapterz.springboot.exception;

public class PostLikeNotFoundException extends RuntimeException {

    public PostLikeNotFoundException() {
        super("좋아요를 누르지 않은 게시글입니다.");
    }
}
