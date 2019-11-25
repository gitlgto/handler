package com.nzxpc.handler.util.db;

import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

@Getter
@Accessors(chain = true)
@MappedSuperclass
public class IdEntityBasePure<T extends IdEntityBasePure> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Min(value = 0, message = "[{display}]参数非法")
    @Max(value = Integer.MAX_VALUE, message = "[{display}]参数非法")
    @Display("序号")
    private int id;

    public T setId(int id) {
        this.id = id;
        return (T) this;
    }
}
