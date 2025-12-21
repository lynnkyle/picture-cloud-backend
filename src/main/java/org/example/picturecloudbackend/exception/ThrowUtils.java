package org.example.picturecloudbackend.exception;

/**
 * 异常处理工具类
 **/
public class ThrowUtils {
    /**
     * 条件成立则抛异常
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     * @param condition
     * @param errorCode
     * @param description
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String description) {
        throwIf(condition, new BusinessException(errorCode, description));
    }
}
