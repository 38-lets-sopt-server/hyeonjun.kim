package org.sopt.common.response;

public record BaseResponse<T>(
	boolean success,
	String message,
	T data
) {
	public static <T> BaseResponse<T> success(T data) {
		return new BaseResponse<>(true, "Success", data);
	}

	public static <T> BaseResponse<T> fail(String message) {
		return new BaseResponse<>(false, message, null);
	}
}
