package com.nzxpc.handler.mem.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@Accessors(chain = true)
public class EntityBase extends EntityBaseNoUpdate {
    /**
     * 修改时间
     */
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime updateAt = LocalDateTime.now();

}
