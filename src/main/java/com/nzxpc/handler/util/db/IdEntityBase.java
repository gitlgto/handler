package com.nzxpc.handler.util.db;

import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * 分继承memId 和Id两种，第一种要加继承类要加JsonIgnoreProperties(忽略类中不存在的字段)，也不一定，同时要想在数据库有实体必须写entity注解
 * @param <T>
 */
@MappedSuperclass
@Getter
@Accessors(chain = true)
public class IdEntityBase<T extends IdEntityBase> extends IdEntityBasePure<T> {
    /**
     * 修改时间
     */
    @Display("修改时间")
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime updateAt = LocalDateTime.now();

    /**
     * 创建时间
     */
    @Display("创建时间")
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime createAt = LocalDateTime.now();

    public T setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
        return (T) this;
    }

    public T setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
        return (T) this;
    }
}
