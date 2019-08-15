package com.nzxpc.handler.mem.core.util;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.google.gson.Gson;
import com.nzxpc.handler.mem.core.entity.Result;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsUtil {
    private final static Map<String, String> config = new HashMap<>();
    private static boolean init = false;

    private static void initConfig() {
        if (!init) {
            config.clear();
            config.put("type", AppPropUtil.get("smsType"));
            if (StringUtils.isBlank(config.get("type"))) {
                config.put("type", "ali");
            }
            switch (config.get("type")) {
                case "zzy":
                    config.put("zzy-uid", AppPropUtil.get("ZzyUid"));
                    config.put("zzy-pwd", AppPropUtil.get("ZzyPwd"));
                    config.put("template", AppPropUtil.get("ZzyTemplateContent"));
                    //国内短信地址
                    config.put("ch_url", "http://service2.winic.org/service.asmx/SendMessages");
                    //国际短信地址
                    config.put("gl_url", "http://service2.winic.org/service.asmx/SendInternationalMessages");
                    config.put("zzy_err_-01", "用户账户余额不足");
                    break;
                case "ytx":
                case "eums":
                default:
                    config.put("ali_access_key", AppPropUtil.get("AccessKeyId"));
            }
            init = true;

        }
    }

    private static Result aliSend(String templateCode, String template, String cotent, String... phoneNumebers) {
        Result ret = new Result();
        final String accessKeyId = config.get("ali_access_key");
        final String accessKeySecret = config.get("ali_secret_key");
        final String signName = config.get("sign_name");
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        final String product = "Dysmsapi";
        final String domain = "dysmsapi.aliyuncs.com";

        HashMap<String, String> map = new HashMap<>();
        parseTemplate(template, cotent, map);
        String paramStr = new Gson().toJson(map);
        IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKeyId, accessKeySecret);
        try {
            DefaultProfile.addEndpoint("cn-shanghai", "cn-shanghai", product, domain);
        } catch (ClientException e) {
            LogUtil.err("", e);
            return ret.setMsg(e.getErrMsg());
        }
        IAcsClient acsClient = new DefaultAcsClient(profile);
        SendSmsRequest request = new SendSmsRequest();
        request.setMethod(MethodType.POST);
        request.setPhoneNumbers(StringUtils.join(phoneNumebers, ""));
        request.setSignName(signName);
        request.setTemplateCode(templateCode);
        request.setTemplateParam(paramStr);
        try {
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            ret.setOk("OK".equals(sendSmsResponse.getCode()));
            ret.setMsg(sendSmsResponse.getMessage());
        } catch (ClientException e) {
            LogUtil.err("", e);
            ret.setMsg(e.getErrMsg());
        }
        return ret;
    }

    private static String parseTemplate(String template, String code, Map<String, String> map) {
        Matcher matcher = Pattern.compile("\\$\\{\\w+}").matcher(template);
        StringBuilder sb = new StringBuilder();
        if (matcher.find()) {
            if (map != null) {
                map.put(matcher.group(1), code);
            }
            matcher.appendReplacement(sb, code);
        }
        while (matcher.find()) {
            if (map != null) {
                map.put(matcher.group(1), "");
            }
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
