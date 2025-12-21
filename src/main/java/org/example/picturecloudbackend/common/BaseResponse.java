package org.example.picturecloudbackend.common;

import org.example.picturecloudbackend.exception.ErrorCode;

import java.io.Serializable;

public class BaseResponse<T> implements Serializable {
    public int code;
    public T data;
    public String message;
    public String description;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(ErrorCode errorCode, T data) {
        this.code = errorCode.getCode();
        this.data = data;
        this.message = errorCode.getMessage();
    }

    public BaseResponse(ErrorCode errorCode, T data, String description) {
        this.code = errorCode.getCode();
        this.data = data;
        this.message = errorCode.getMessage();
        this.description = description;
    }
}
