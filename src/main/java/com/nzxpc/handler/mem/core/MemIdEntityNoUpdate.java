package com.nzxpc.handler.mem.core;

import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * 日终落地的实体Id去除了主键属性，防止由于数据库数据未彻底清除，导致主键重复引起落地失败
 * @param <T>
 */
@MappedSuperclass
@Getter
@Accessors(chain = true)
public class MemIdEntityNoUpdate<T extends MemIdEntityNoUpdate> extends MemIdEntityPure<T> {
    @Display("创建时间")
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime createAt = LocalDateTime.now();

    public T setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
        return (T) this;
    }
}
