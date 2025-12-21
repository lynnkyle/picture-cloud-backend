package org.example.picturecloudbackend.exception;

import org.example.picturecloudbackend.common.BaseResponse;
import org.example.picturecloudbackend.common.ResultUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> exceptionHandler(Exception e) {
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
