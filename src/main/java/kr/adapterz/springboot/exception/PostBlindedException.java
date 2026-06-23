package kr.adapterz.springboot.exception;

public class PostBlindedException extends RuntimeException {

    public PostBlindedException() {
        super("블라인드 처리된 게시글입니다.");
    }
}
