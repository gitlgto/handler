package com.nzxpc.handler.mem.core.entity;

import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Accessors(chain = true)
public class EntityBase<T extends EntityBase> extends EntityBaseNoUpdate<T> {
    /**
     * 修改时间
     */
    @Display("修改时间")
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime updateAt = LocalDateTime.now();

    public T setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
        return (T) this;
    }

}
