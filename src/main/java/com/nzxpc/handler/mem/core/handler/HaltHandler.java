package com.nzxpc.handler.mem.core.handler;

import com.nzxpc.handler.mem.core.IHandler;
import com.nzxpc.handler.mem.core.entity.EventModelBase;
import com.nzxpc.handler.mem.core.entity.Result;

public class HaltHandler implements IHandler<EventModelBase> {
    @Override
    public Result process(EventModelBase model, boolean isRuntime) {

        return null;
    }
}
