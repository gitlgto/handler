package com.nzxpc.handler.mem.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
@MappedSuperclass
public class IdEntityBasePure implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Min(value = 0, message = "参数非法")
    @Max(value = Integer.MAX_VALUE, message = "参数非法")
    private int id;

}
