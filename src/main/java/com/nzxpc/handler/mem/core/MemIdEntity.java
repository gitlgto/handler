package com.nzxpc.handler.mem.core;

import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@Accessors(chain = true)
public class MemIdEntity<T extends MemIdEntity> extends MemIdEntityPure<T> {
    @Display("创建时间")
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime createAt = LocalDateTime.now();

    @Display("更新时间")
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime updateAt = LocalDateTime.now();
}
