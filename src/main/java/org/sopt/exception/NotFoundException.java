package org.sopt.exception;

public class NotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // @Getter가 자동으로 만들어주던 getter를 직접 작성
    public ErrorCode getErrorCode() { return errorCode; }
}