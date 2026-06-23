package kr.adapterz.springboot.exception;

public class PostDraftNotFoundException extends RuntimeException {

    public PostDraftNotFoundException() {
        super("임시저장을 찾을 수 없습니다.");
    }
}
