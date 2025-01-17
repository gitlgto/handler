package com.nzxpc.handler.util.db;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DatetimeListModelIn<T> extends PageBean<T> {
    private LocalDateTime from;
    private LocalDateTime to;
}
