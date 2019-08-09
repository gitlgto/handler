package com.nzxpc.handler.mem.core;

import java.util.Map;

public class IdHolder {
    static Map<String, Integer> IdMap;

    public static int newId(Class clazz) {
        int newId;
        if (IdMap.containsKey(clazz.getName())) {
            newId = IdMap.get(clazz.getName()) + 1;
        } else {
            newId = 887;
        }
        IdMap.put(clazz.getName(), newId);
        return newId;
    }
}
