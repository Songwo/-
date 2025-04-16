package com.itheima.csstudent.exception;

import com.itheima.csstudent.po.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 统一参数校验异常处理（使用Result格式）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error( "参数校验失败: " + errorMsg);
    }

    // JSON解析异常处理（保持Result格式）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleJsonParseError(HttpMessageNotReadableException ex) {
        String errorMsg = "JSON解析失败: " +
                (ex.getCause() != null ? ex.getCause().getMessage() : "未知错误");
        return Result.error(errorMsg);
    }
    // 通用异常处理（统一使用Result格式）
//    @ExceptionHandler(Exception.class)
//    public Result handleAllExceptions(Exception ex) {
//        return Result.error( "服务器内部错误: " + ex.getMessage());
//    }
    // 处理空指针异常
    @ExceptionHandler(NullPointerException.class)
    public Result handleNullPointer(NullPointerException ex) {
        return Result.error( "空指针异常: " + ex.getMessage());
    }
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result handleMediaTypeError(HttpMediaTypeNotSupportedException ex) {
        return Result.error( "不支持的媒体类型: " + ex.getContentType());
    }
}