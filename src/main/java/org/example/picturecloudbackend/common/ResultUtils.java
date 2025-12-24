package org.example.picturecloudbackend.common;

import org.example.picturecloudbackend.exception.ErrorCode;

public class ResultUtils {

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS, data);
    }

    public static <T> BaseResponse<T> success(int code, T data, String message, String description) {
        return new BaseResponse<>(code, data, message, description);
    }

    public static <T> BaseResponse<T> success(ErrorCode errorCode, T data) {
        return new BaseResponse<>(errorCode.getCode(), data, errorCode.getMessage(), "");
    }

    public static <T> BaseResponse<T> success(ErrorCode errorCode, T data, String description) {
        return new BaseResponse<>(errorCode.getCode(), data, errorCode.getMessage(), description);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message, "");
    }

    public static <T> BaseResponse<T> error(int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage(), "");
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage(), description);
    }
}
