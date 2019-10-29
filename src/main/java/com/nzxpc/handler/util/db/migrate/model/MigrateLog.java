package com.nzxpc.handler.util.db.migrate.model;

import com.nzxpc.handler.util.db.migrate.annotation.Migrate;
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

    @Migrate(oldName = "migrateSql")
    @Column(columnDefinition = "text")
    private String sql;

    private boolean success;
    private LocalDateTime createAt = LocalDateTime.now();
}
