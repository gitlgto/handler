package com.nzxpc.handler.mem.core;

import com.nzxpc.handler.util.db.migrate.annotation.IndexColumn;
import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@MappedSuperclass
@Accessors(chain = true)
public class MemIdEntityPure<T extends MemIdEntityPure> {
    @IndexColumn
    @Min(value = 0, message = "[{display}]参数非法")
    @Max(value = Integer.MAX_VALUE, message = "[{display}]参数非法")
    @Display("编号")
    private int id;

    public T setId(int id) {
        this.id = id;
        return (T) this;
    }

    //增加主键字段，以备手动处理db之需
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int pkey;
}
