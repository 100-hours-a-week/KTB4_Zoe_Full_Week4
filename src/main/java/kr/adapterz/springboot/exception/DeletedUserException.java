package kr.adapterz.springboot.exception;

public class DeletedUserException extends RuntimeException {

    public DeletedUserException() {
        super("탈퇴한 사용자입니다.");
    }
}
