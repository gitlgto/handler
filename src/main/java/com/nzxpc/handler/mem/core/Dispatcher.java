package com.nzxpc.handler.mem.core;

import com.nzxpc.handler.mem.core.entity.DefaultEventModel;
import com.nzxpc.handler.mem.core.entity.EventModelBase;
import com.nzxpc.handler.mem.core.entity.OpType;
import com.nzxpc.handler.mem.core.entity.Result;
import com.nzxpc.handler.mem.core.util.ThreadUtil;
import com.nzxpc.handler.mem.core.util.Util;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Dispatcher {
    private static volatile boolean running = true;
    private static ConcurrentHashMap<String, Result> httpResultMap = new ConcurrentHashMap<>();
    private static ArrayBlockingQueue<EventModelBase> inputEventQueue = new ArrayBlockingQueue<>(100000);
    private static ArrayBlockingQueue<EventModelBase> outputEventQueue = new ArrayBlockingQueue<>(100000);
    private static ConcurrentHashMap<AfterDoEventListener, Boolean> listeners = new ConcurrentHashMap<>();
    private static Dispatcher instance = new Dispatcher();

    public static Dispatcher instance() {
        return instance;
    }

    public static void removeListener(AfterDoEventListener item) {
        listeners.remove(item);
    }

    public Dispatcher addEventListener(AfterDoEventListener... listener) {
        for (AfterDoEventListener item : listener) {
            listeners.put(item, true);
        }
        return instance;
    }

    public Dispatcher addEndListener(EndListener listener) {
        EndManager.Listeners.add(listener);
        return instance;
    }

    public static long getInputSize() {
        return inputEventQueue.size();
    }

    public static long getOutputSize() {
        return outputEventQueue.size();
    }

    public Dispatcher start() {
        EndManager.start();
        ThreadUtil.start(() -> {
            while (true) {
                try {
                    EventModelBase event = inputEventQueue.take();
                    if (running) {
                        long start = System.nanoTime();
                        Mem.doEvent(event, true);
                        RunStateManager.calcOpTime(System.nanoTime() - start, event.getType(), OpType.Handler);
                    } else {
                        event.setMsg("系统维护中").setOk(false);
                    }
                    outputEventQueue.put(event);
                } catch (Throwable e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }

        }).setName("event-input");
        ThreadUtil.start(() -> {
            while (true) {
                try {
                    EventModelBase event = outputEventQueue.take();
                    afterDoEvent(event);
                } catch (Throwable e) {
                    System.out.println(e);
                    System.exit(0);
                }
            }
        }).setName("event-output");
        return instance;
    }

    public static void stop() {
        running = false;
    }

    public static boolean isRunning() {
        return running;
    }

    public Dispatcher setEndAt(LocalTime endAt) {
        EndManager.EndAt = endAt;
        return instance;
    }

    public static LocalTime getEndAt() {
        return EndManager.EndAt;
    }

    private void afterDoEvent(EventModelBase event) {
        if (event.isSync()) {
            httpResultMap.put(event.getSrData(), new Result(event.isOk(), event.getMsg()).setCode(event.getCode()).setExtend(event.getExtend()));
            if (event.isSyncToGateway()) {
                for (AfterDoEventListener listener : listeners.keySet()) {
                    listener.afterDoEvent(event);
                }
            }
        } else {
            for (AfterDoEventListener listener : listeners.keySet()) {
                listener.afterDoEvent(event);
            }
        }
    }

    public static Result syncCall(EventModelBase event) {
        event.setSrData(Util.uuid()).setSync(true);
        Result result = call(event);
        if (!result.isOk()) {
            return result;
        }
        return tryGetSyncResult(event.getSrData());
    }

    public static <T extends IHandler> Result syncCall(DefaultEventModel event, Class<T> handlerClass) {
        event.setSrData(Util.uuid()).setSync(true);
        Result result = call(event, handlerClass);
        if (!result.isOk()) {
            return result;
        }
        return tryGetSyncResult(event.getSrData());
    }


    public static <T extends IHandler> Result call(DefaultEventModel model, Class<T> handlerClass) {
        model.setType(Mem.handler2Type(handlerClass, false));
        return realCall(model);
    }

    public static Result call(EventModelBase model) {
        if (model.getClass() == DefaultEventModel.class) {
            if (model.getType() == null) {
                System.out.println("Dispatcher.call方法不支持" + DefaultEventModel.class.getSimpleName() + "作为参数：" + model.toString());
            }
        } else {
            model.setType(Mem.model2Type(model.getClass(), false));
        }
        return realCall(model);
    }

    private static Result realCall(EventModelBase model) {
        if (!sysEvents.contains(model.getType())) {
            if (model.getSrData() == null) {
                System.out.println(model.getClass().getSimpleName() + "的属性srData不能为空");
                System.exit(0);
            }
            if (model.getDoerId() <= 0 || StringUtils.isBlank(model.getDoerIp())) {
                return new Result(false, "操作者信息不完整");
            }
        }
        try {
            inputEventQueue.put(model);
        } catch (InterruptedException e) {
            System.out.println(e);
            System.exit(0);
        }
        return new Result(true, "正在处理，请稍后....");
    }

    private static Result tryGetSyncResult(String srData) {
        int maxCnt = 500;
        int interVal = 5;
        int i = 0;
        try {
            while (true) {
                Thread.sleep(interVal);
                Result ret = httpResultMap.remove(srData);
                if (ret != null) {
                    return ret;
                } else if (i++ > maxCnt) {
                    return new Result(false, "操作超时");
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e);
            System.exit(0);
            return new Result(false, e.getMessage());
        }
    }


    private static Set<String> sysEvents = new HashSet<>();

    public Dispatcher addSysEvents(Class... handlerClass) {
        for (Class item : handlerClass) {
            sysEvents.add(Mem.handler2Type(item, true));
        }
        return instance;
    }

    public static void initCall(EventModelBase model) {
        model.setDoerInfo(1, "-");
        Result result = syncCall(model);
        if (!result.isOk()) {
            System.exit(0);
            throw new RuntimeException(result.getMsg());
        }
    }

    public static void initCall(EventModelBase model, Class handlerClass) {
        String type = Mem.handler2Type(handlerClass, false);
        initCall(model.setType(type));
    }

}
