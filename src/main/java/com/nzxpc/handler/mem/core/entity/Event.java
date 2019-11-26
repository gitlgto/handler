package com.nzxpc.handler.mem.core.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nzxpc.handler.util.db.IdEntityBaseNoUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Max;
import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Event extends IdEntityBaseNoUpdate {
    private int doerId;
    @Max(value = 30)
    private String ip;
    private String type;
    @Column(columnDefinition = "datetime(3)")
    private LocalDateTime time;
    @Column(columnDefinition = "longtext")
    private String content;

    public Event(EventModelBase model) {
        doerId = model.getDoerId();
        ip = model.getDoerIp();
        type = model.getType();
        time = model.nowTime();
        try {
            content = new ObjectMapper().findAndRegisterModules().writeValueAsString(model);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
