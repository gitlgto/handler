package com.nzxpc.handler.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nzxpc.handler.util.db.IdEntityBasePure;
import com.nzxpc.handler.util.db.migrate.annotation.IndexColumn;
import com.nzxpc.handler.util.validate.Display;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Button extends IdEntityBasePure {
    /**
     * 名称
     */
    @Display("名称")
    @Size(max = 100)
    @JsonProperty("text")
    private String name;

    @Display("上级")
    @Size(max = 300)
    private String parentCode;

    /**
     * 代码,自动生成,唯一
     */
    @Size(max = 100)
    @NotNull
    private String code;

    /**
     * /controller/action
     */
    @Display("链接")
    @Size(max = 256)
    @IndexColumn
    private String uri;

    /**
     * true：菜单或菜单组
     */
    @Display("是否是菜单")
    @NotNull
    private boolean menu;

    /**
     * button图标
     */
    @Size(max = 100)
    private String imgClass;


    /**
     * 顺序,从大到小，越大越靠前
     */
    @NotNull
    @Digits(integer = 13, fraction = 8)
    private double orderNum;

    /**
     * true：只要登录就有权进行操作
     */
    @NotNull
    private boolean freeForWorker;

    /**
     * true：不登录也有权限进行操作
     */
    @NotNull
    private boolean freeForVisitor;

    /**
     * true：系统级,仅管理员拥有权限,无法分配
     */
    @Display("是否是系统级")
    @NotNull
    private boolean onlyForSystem;

    /**
     * true 被删除了
     */

    @Display("是否禁用")
    @NotNull
    private boolean deleted;

    @Display("默认参数")
    private String defaultParams;
}
