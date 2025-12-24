package com.classroom.common.exception;

import com.classroom.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统异常", e);
        return R.error("系统繁忙，请稍后重试");
    }
}
