package com.nzxpc.handler.mem.core;

import com.nzxpc.handler.mem.core.handler.EndHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class EndManager {
    private final static ThreadPoolTaskScheduler endScheduler = new ThreadPoolTaskScheduler();
    static LocalTime EndAt = LocalTime.of(5, 0);
    public static Set<EndListener> Listeners = new HashSet<>();

    private static String getCron() {
        return String.format("%s %s %s * *", EndAt.getSecond(), EndAt.getMinute(), EndAt.getHour());
    }

    static void start() {
        Dispatcher.instance().addSysEvents(EndHandler.class);
        endScheduler.setDaemon(true);
        endScheduler.initialize();
        endScheduler.schedule(EndManager::endClear, new CronTrigger(getCron()));
    }

    private static void endClear() {

    }

}
