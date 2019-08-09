package com.nzxpc.handler.mem.core;

import com.nzxpc.handler.mem.core.entity.Result;

public interface IHandler<T> {
    abstract Result process(T model, boolean isRuntime);
}
