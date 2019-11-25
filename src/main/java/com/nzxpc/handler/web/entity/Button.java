package com.nzxpc.handler.web.entity;

import com.nzxpc.handler.util.db.IdEntityBasePure;
import com.nzxpc.handler.util.validate.Display;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Button extends IdEntityBasePure {
    @Display("上级")
    @Size(max = 300)
    private String parentCode;
}
