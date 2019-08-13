package com.nzxpc.handler.mem.core.util;

import java.util.UUID;

public class Util {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
