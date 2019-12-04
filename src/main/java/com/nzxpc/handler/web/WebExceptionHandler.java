package com.nzxpc.handler.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nzxpc.handler.mem.core.entity.Result;
import com.nzxpc.handler.util.LogUtil;
import com.nzxpc.handler.util.validate.Display;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class WebExceptionHandler {
    private Object dealReturn(Result r) {
        return r;
    }

    /**
     * 参数验证异常
     */
    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public Object handleBindException(BindException bindException) {
        Object target = bindException.getTarget();
        FieldError fieldError = bindException.getFieldError();
        String message = fieldError == null ? "未知错误" : fieldError.getDefaultMessage();
        if (StringUtils.isNotBlank(message) && message.contains("{display}")) {
            if (target == null) {
                return dealReturn(new Result(false, "未知错误"));
            }
            try {
                String property = fieldError.getField();
                Field field = target.getClass().getDeclaredField(property);
                Display displayAnnotation = field.getDeclaredAnnotation(Display.class);
                if (displayAnnotation != null) {
                    String display = displayAnnotation.value();
                    message = StringUtils.replace(message, "{display}", display);
                } else {
                    message = StringUtils.replace(message, "{display}", property);
                }
            } catch (NoSuchFieldException | SecurityException e1) {
                LogUtil.err(target.getClass(), e1);
            }
        }
        if (fieldError != null) {
            Class<?> clz = bindException.getFieldType(fieldError.getField());
            if (clz != null && clz.isEnum()) {
                List<String> filedNameList = new ArrayList<>();
                for (Field item : clz.getFields()) {
                    filedNameList.add(item.getName());
                }
                if (fieldError.getRejectedValue() != null && !filedNameList.contains(String.valueOf(fieldError.getRejectedValue()))) {
                    var r = new Result(false, "参数" + fieldError.getField() + "的值" + fieldError.getRejectedValue() + "不在" + filedNameList.toString() + "内");
                    return dealReturn(r);
                }
            }
        }

        var r = new Result(false, message);
        return dealReturn(r);
    }

    /**
     * 请求方式异常
     */
    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Object handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException err) {
        String msg = "请求方式错误：不支持[" + err.getMethod() + "]请求";
        LogUtil.err(msg);
        var r = new Result(false, msg);
        return dealReturn(r);
    }

    @ResponseBody
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameterException(MissingServletRequestParameterException err) {
        String msg = "必填项不能为空 : 参数[" + err.getParameterName() + "], 类型[" + err.getParameterType() + "]";
        LogUtil.err(msg);
        var r = new Result(false, msg);
        return dealReturn(r);
    }


    @Autowired
    private HttpServletRequest request;

    /**
     * 总异常处理
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object handleException(Exception err) {
        try {
            LogUtil.err("[" + request.getRequestURI() + "]: " + new ObjectMapper().findAndRegisterModules().writeValueAsString(request.getParameterMap()));
        } catch (JsonProcessingException ignore) {
        }
        LogUtil.err(err);
        var r = new Result(false, "系统异常:" + err.getMessage());
        return dealReturn(r);
    }
}
