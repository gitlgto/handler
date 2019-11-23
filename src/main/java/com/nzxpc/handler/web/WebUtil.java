package com.nzxpc.handler.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nzxpc.handler.util.LogUtil;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class WebUtil {
    private final static UrlPathHelper PH = new UrlPathHelper();

    public static String uri(HttpServletRequest request) {
        return PH.getOriginatingRequestUri(request);
    }

    private static boolean end(int code, String msg, boolean clearSession) throws IOException {
        var ctx = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes());
        if (clearSession) {
            ctx.getRequest().getSession().invalidate();
        }
        var response = ctx.getResponse();
        response.setStatus(code);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(msg);
        return false;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ResponseModel {
        private int status;
        private String message;


        public static String SessionGone;
        public static String NoAuth;

        static {
            try {
                SessionGone = new ObjectMapper().findAndRegisterModules().writeValueAsString(new ResponseModel(401, "会话过期请重新登录"));
                NoAuth = new ObjectMapper().findAndRegisterModules().writeValueAsString(new ResponseModel(403, "无权操作"));
            } catch (JsonProcessingException e) {
                LogUtil.err("", e);
            }
        }
    }

    public static boolean sessionGone() throws IOException {
        return end(401, ResponseModel.SessionGone, true);
    }

    public static boolean noAuth() throws IOException {
        return noAuth(ResponseModel.NoAuth);
    }

    public static boolean noAuth(String msg) throws IOException {
        return end(403, msg, false);
    }
}
