package com.nzxpc.handler.mem.core.entity;

import com.nzxpc.handler.util.StrUtil;
import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Accessors(chain = true)
public class EntityBaseNoUpdate<T extends EntityBaseNoUpdate> implements Serializable {
    /**
     * 创建时间
     */
    @Display("创建时间")
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime createAt = LocalDateTime.now();

    public T setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
        return (T) this;
    }

    @Override
    public String toString() {
        return StrUtil.toStr(this);
    }
}
