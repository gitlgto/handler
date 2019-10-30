package com.nzxpc.handler.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.time.LocalDateTime;

/**
 * 定时任务工具
 */
public class ScheduleUtil {

    // TODO ThreadPoolTaskScheduler
    // TODO 1.schedule(Runnable task, Date stateTime)在指定时间执行一次定时任务 2.schedule(Runnable task, Trigger trigger)动态创建指定表达式cron 的定时任务，
    // TODO tr.schedule(()->{},triggerContext->newCroTrigger("").nextExecutionTime(triggerContext))
    // TODO scheduleAtFixedRate,指定间隔时间执行一次任务，间隔时间为前一次执行开始到下次任务开始时间
    // TODO scheduleWithFixedDelay,指定间隔时间执行一次任务，间隔时间为前一次任务完成到下次开始时间

    //新创建线程名称前缀 创建守护线程 初始化 设置bean name

    private final static ThreadPoolTaskScheduler SCHEDULER = new ThreadPoolTaskScheduler();

    private static String getCron(int hour, int minute, int second) {
        return String.format("%s %s %s * * ?", second, minute, hour);
    }

    static {
        SCHEDULER.setThreadNamePrefix("ScheduleUtil");
        SCHEDULER.setDaemon(true);
        SCHEDULER.setBeanName("定时任务请使用ScheduleUtil");
        SCHEDULER.initialize();
    }

    /**
     * 指定时分秒，每天都会执行一次
     */
    public static void addDayWork(Runnable task, int hour, int minute, int second) {
        SCHEDULER.schedule(task, new CronTrigger(getCron(hour, minute, second)));
    }

    public static void addDayWork(Runnable task, LocalDateTime time) {
        SCHEDULER.schedule(task, new CronTrigger(getCron(time.getHour(), time.getMinute(), time.getSecond())));
    }

    /**
     * 每隔n分钟执行一次
     */
    public static void addMinuteWork(Runnable task, int minutes) {
        if (minutes <= 0 || minutes >= 60) {
            throw new RuntimeException("分钟数只能在【1,59】之间");
        }
        if ((minutes + "").startsWith("0")) {
            throw new RuntimeException("不能以0开头");
        }
        SCHEDULER.schedule(task, new CronTrigger("0 0/" + minutes + " * * * ?"));
    }
}
