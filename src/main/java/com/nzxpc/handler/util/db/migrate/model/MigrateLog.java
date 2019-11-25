package com.nzxpc.handler.util.db.migrate.model;

import com.nzxpc.handler.util.db.migrate.annotation.Migrate;
import com.nzxpc.handler.util.validate.Display;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class MigrateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Display("SQL语句")
    @Migrate(oldName = "migrateSql")
    @Column(columnDefinition = "text")
    private String sql;

    @Display("是否成功")
    private boolean success;
    @Display("创建时间")
    private LocalDateTime createAt = LocalDateTime.now();
}
