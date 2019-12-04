package com.nzxpc.handler.util;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.web.multipart.MultipartFile;

public class Constant {
    public static ObjectMapper JacksonMapper = new ObjectMapper().findAndRegisterModules();

    static {
        SimpleModule module = new SimpleModule();
        JacksonMapper.registerModule(module);
        JacksonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        JacksonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //全局配置：忽略所有MultipartFile类型的属性
        JacksonMapper.addMixIn(MultipartFile.class, MyMixInForIgnoreType.class);
    }

    @JsonIgnoreType
    public class MyMixInForIgnoreType {
    }
}
