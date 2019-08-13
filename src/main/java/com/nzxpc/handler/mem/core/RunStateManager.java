package com.nzxpc.handler.mem.core;

import com.nzxpc.handler.mem.core.entity.CalcModel;
import com.nzxpc.handler.mem.core.entity.OpType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RunStateManager {
    public static ConcurrentHashMap<String, Map<String, CalcModel>> HandlerTimeMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Map<String, CalcModel>> OtherTimeMap = new ConcurrentHashMap<>();

    public static void calcOpTime(long interval, String op, OpType type) {
        ConcurrentHashMap<String, Map<String, CalcModel>> map = null;
        switch (type) {
            case Handler:
                map = HandlerTimeMap;
                break;
            case Other:
                map = OtherTimeMap;
                break;
        }
        Map<String, CalcModel> innerMap = map.computeIfAbsent(Thread.currentThread().getName(), k -> new HashMap<>());
        CalcModel model = innerMap.computeIfPresent(op, (k, v) -> {
            long l = v.getTotalCount() + 1;
            v.setTotalCount(l);
            v.setLast(interval);
            v.setLastAt(System.currentTimeMillis());
            v.setTotalTime(v.getTotalTime() + interval);
            return v;
        });
        if (model == null || model.getTotalTime() == Float.POSITIVE_INFINITY) {
            long now = System.currentTimeMillis();
            innerMap.put(op, new CalcModel(1, interval, interval, interval, now, now));
        }

    }
}
