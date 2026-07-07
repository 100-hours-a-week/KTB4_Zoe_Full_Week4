package kr.adapterz.springboot.exception;

public class DeletedCommentException extends RuntimeException {

    public DeletedCommentException() {
        super("삭제된 댓글은 수정할 수 없습니다.");
    }
}
