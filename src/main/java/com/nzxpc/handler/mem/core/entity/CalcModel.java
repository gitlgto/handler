package com.nzxpc.handler.mem.core.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CalcModel {
    private long totalCount;
    private float totalTime;
    private long first;
    private long last;
    private long lastAt;
    private long firstAt;
}
