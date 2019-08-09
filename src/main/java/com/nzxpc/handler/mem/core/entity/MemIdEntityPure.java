package com.nzxpc.handler.mem.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@Accessors(chain = true)
@MappedSuperclass
public class MemIdEntityPure {

    @IndexColumn
    @Min(value = 0, message = "参数非法")
    @Max(value = Integer.MAX_VALUE, message = "参数非法")
    private int id;
}
