package kr.adapterz.springboot.exception;

public class ImageUploadFailedException extends RuntimeException {

    public ImageUploadFailedException(Throwable cause) {
        super(cause);
    }
}
