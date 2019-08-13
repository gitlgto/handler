package com.nzxpc.handler.mem.core.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class StopWatch {
    private String beginMsg;
    private String endMsg;
    private long ns;

    private StopWatch(String beginMsg, long ns) {
        this.beginMsg = beginMsg;
        this.ns = ns;
        //打印自定义信息
        System.out.println("begin" + beginMsg);
    }

    private StopWatch(long ns) {
        this.ns = ns;
    }

    public static StopWatch startNew(String beginMsg) {
        return new StopWatch(beginMsg, System.nanoTime());
    }

    public void restart(String beginMsg) {
        this.beginMsg = beginMsg;
        this.ns = System.nanoTime();
        System.out.println("begin" + beginMsg);
    }

    public void restart() {
        this.ns = System.nanoTime();
    }

    public static StopWatch startNew() {
        return new StopWatch(System.nanoTime());
    }

    public void end(String endMsg) {
        long l = System.nanoTime() - ns;
        float ms = l / 1000000f;
        System.out.println(String.format("end" + endMsg + "【%s ms,%s ns】", ms, l));
    }

    public void end() {
        long l = System.nanoTime() - ns;
        float ms = l / 1000000f;
        System.out.println(String.format("end:【%s ms,%s ns】", ms, l));
    }

}
