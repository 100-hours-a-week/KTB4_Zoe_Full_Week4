package kr.adapterz.springboot.exception;

public class ParentCommentMismatchException extends RuntimeException {

    public ParentCommentMismatchException() {
        super("부모 댓글이 게시글에 속하지 않습니다.");
    }
}
