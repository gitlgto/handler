package com.nzxpc.handler.util;

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

    public static Thread startTasks(int second, Runnable... runnables) {
        if (second <= 0 || runnables == null || runnables.length <= 0) {
            throw new RuntimeException("参数错误");
        }
        Thread th = new Thread(() -> {
            while (true) {
                try {
                    for (Runnable runnable : runnables) {
                        runnable.run();
                    }
                    Thread.sleep(second * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        th.setDaemon(true);
        th.start();
        th.setName("");
        return th;
    }
}
