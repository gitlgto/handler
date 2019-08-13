package com.nzxpc.handler.mem.core.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ThreadUtil {

    public static Thread start(Runnable lamM) {
        Thread thread = new Thread(lamM);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}
