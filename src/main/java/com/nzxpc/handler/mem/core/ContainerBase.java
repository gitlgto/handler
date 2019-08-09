package com.nzxpc.handler.mem.core;

import java.util.HashMap;

public abstract class ContainerBase {
    public HashMap<String, Integer> IdMap = new HashMap<>();

    public ContainerBase() {
        IdHolder.IdMap = IdMap;
    }
}
