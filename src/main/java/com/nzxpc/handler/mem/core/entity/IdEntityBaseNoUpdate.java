package com.nzxpc.handler.mem.core.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@MappedSuperclass
public class IdEntityBaseNoUpdate extends IdEntityBasePure {
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime createAt = LocalDateTime.now();
}
