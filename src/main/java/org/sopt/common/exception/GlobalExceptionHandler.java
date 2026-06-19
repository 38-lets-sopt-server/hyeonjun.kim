package org.sopt.common.exception;

import org.sopt.common.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<BaseResponse<Void>> handleNotFoundException(NotFoundException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity.status(errorCode.getStatus())
			.body(BaseResponse.fail(errorCode.getMessage()));
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<BaseResponse<Void>> handleBadRequestException(BadRequestException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity.status(errorCode.getStatus())
			.body(BaseResponse.fail(errorCode.getMessage()));
	}
}
