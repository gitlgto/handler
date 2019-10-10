package com.nzxpc.handler.mem.core;

public interface ICache {
    /**
     *重放前执行
     */
    void beforeReplay();

    /**
     *事件重放后执行，重放后的初始化
     */
    void afterReplay();
}
