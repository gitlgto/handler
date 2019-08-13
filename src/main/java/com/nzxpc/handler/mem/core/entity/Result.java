package com.nzxpc.handler.mem.core.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Getter
@Setter
public class Result {
    private boolean ok;
    private String msg;
    private int code;
    private String extend;

    public Result(boolean ok, String msg) {
        this.ok = ok;
        this.msg = msg;
    }
}
