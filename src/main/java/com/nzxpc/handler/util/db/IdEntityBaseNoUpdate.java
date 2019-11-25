package com.nzxpc.handler.util.db;

import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Accessors(chain = true)
public class IdEntityBaseNoUpdate<T extends IdEntityBaseNoUpdate> extends IdEntityBasePure<T> {

    @Display("创建时间")
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime createAt = LocalDateTime.now();

    public T setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
        return (T) this;
    }

}
