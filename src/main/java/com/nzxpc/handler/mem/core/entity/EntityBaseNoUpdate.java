package com.nzxpc.handler.mem.core.entity;

import com.nzxpc.handler.mem.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@Accessors(chain = true)
public class EntityBaseNoUpdate implements Serializable {
    /**
     * 创建时间
     */

    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime createAt = LocalDateTime.now();

//    @Override
//    public String toString() {
//        return StrUtil.toStr(this);
//    }
}
