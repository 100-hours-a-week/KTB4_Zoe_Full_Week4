package kr.adapterz.springboot.exception;

public class MyPageReadFailedException extends RuntimeException {

    public MyPageReadFailedException(Throwable cause) {
        super(cause);
    }
}
