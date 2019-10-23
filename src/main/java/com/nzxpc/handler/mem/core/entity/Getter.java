package com.nzxpc.handler.mem.core.entity;

import java.io.Serializable;

public interface Getter<T, V> extends Serializable {
    V apply(T bean);
}
