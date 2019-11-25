package com.nzxpc.handler.mem.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nzxpc.handler.mem.core.MemIdEntityPure;
import com.nzxpc.handler.util.IpUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public abstract class EventModelBase {
    private String type;
    private String srData;
    private int doerId;
    private String doerIp;
    private boolean ok;
    private String msg;
    private boolean syncToGateway;
    private int code;
    private String extend;
    private boolean sync;
    private long now;
    private long nano;

    public EventModelBase(String srData, boolean ok, String msg) {
        this.srData = srData;
        this.ok = ok;
        this.msg = msg;
    }

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private LocalDateTime nowTime;

    public LocalDateTime nowTime() {
        if (nowTime == null) {
            nowTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault());
        }
        return nowTime;
    }

    public EventModelBase(String srData, int doerId, String doerIp) {
        this.doerId = doerId;
        this.doerIp = doerIp;
        this.srData = srData;
    }

    public EventModelBase(int doerId, String doerIp) {
        this.doerIp = doerIp;
        this.doerId = doerId;
    }

    @JsonIgnore
    public EventModelBase setDoerInfo(int doerId, String doerIp) {
        this.doerId = doerId;
        this.doerIp = doerIp;
        return this;
    }

    @JsonIgnore
    public EventModelBase setDoerInfo(HttpServletRequest request, int doerId) {
        return setDoerInfo(doerId, IpUtil.getIp(request));
    }

    @JsonIgnore
    public EventModelBase setDoerInfo(HttpServletRequest request, MemIdEntityPure currentUser) {
        return setDoerInfo(currentUser.getId(), IpUtil.getIp(request));
    }

    @JsonIgnore
    public EventModelBase setDoerInfo(HttpServletRequest request) {
        doerIp = IpUtil.getIp(request);
        return this;
    }
}
