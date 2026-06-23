package kr.adapterz.springboot.exception;

public class DuplicatePostReportException extends RuntimeException {

    public DuplicatePostReportException() {
        super("이미 신고한 게시글입니다.");
    }
}
