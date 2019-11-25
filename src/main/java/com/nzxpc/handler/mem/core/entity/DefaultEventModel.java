package com.nzxpc.handler.mem.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nzxpc.handler.mem.core.MemIdEntityPure;
import com.nzxpc.handler.util.IpUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class DefaultEventModel extends EventModelBase {
    @Min(value = 0, message = "参数非法")
    private int id = 0;

    private List<Integer> ids;

    public DefaultEventModel(int id) {
        this.id = id;
    }

    public DefaultEventModel(List<Integer> ids) {
        this.ids = ids;
    }

    public DefaultEventModel(int doerId, String doerIp) {
        super(doerId, doerIp);
    }

    public DefaultEventModel(String srData, int doerId, String doerIp) {
        super(srData, doerId, doerIp);
    }

    public DefaultEventModel(int id, String srData, int doerId, String doerIp) {
        super(srData, doerId, doerIp);
        this.id = id;
    }

    public DefaultEventModel(int id, HttpServletRequest request, MemIdEntityPure currentUser) {
        setDoerInfo(currentUser.getId(), IpUtil.getIp(request));
        this.id = id;

    }

    public DefaultEventModel(int id, HttpServletRequest request, MemIdEntityPure currentUser, boolean syncToGateway) {
        setDoerInfo(currentUser.getId(), IpUtil.getIp(request));
        this.id = id;
        this.setSyncToGateway(syncToGateway);
    }

    public DefaultEventModel(HttpServletRequest request, MemIdEntityPure currentUser, List<Integer> ids) {
        setDoerInfo(currentUser.getId(), IpUtil.getIp(request));
        this.ids = ids;
    }

}
