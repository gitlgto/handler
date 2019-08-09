package com.nzxpc.handler.mem.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public abstract class DbSaverBase<T> {
    public abstract Collection<T> prepareData();

    final public void save() {

    }

    final Class<?> getDataClass() {
        Type type = getClass().getGenericSuperclass();
        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        return (Class) types[0];
    }

    protected Collection<T> oldData;
    protected Collection<T> allData;

    final public void clear() {
        if (oldData != null) {
            allData.removeAll(oldData);
            oldData.clear();
            oldData = null;
            allData = null;
        }
    }
}
