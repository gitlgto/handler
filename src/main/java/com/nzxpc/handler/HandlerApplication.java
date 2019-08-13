package com.nzxpc.handler;

import com.nzxpc.handler.mem.core.Container;
import com.nzxpc.handler.mem.core.Dispatcher;
import com.nzxpc.handler.mem.core.Mem;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalTime;

@SpringBootApplication
@EnableScheduling
public class HandlerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(HandlerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LocalTime time = LocalTime.of(5, 0);
//        Mem.newContainer(Container.class).init()
//        Dispatcher.instance().setEndAt(time).addEndListener().addSysEvents().start();
    }
}
