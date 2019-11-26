package com.nzxpc.handler.web;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ButtonAttribute {
    /**
     * 操作名称
     */
    String name();

    /**
     * 图标
     */
    String icon() default "";

    /**
     * 排序字段，越大靠靠前
     */
    double orderNum() default 0;

    /**
     * 是否是菜单
     */
    boolean isMenu() default false;

    /**
     * 是否对所有人开放, 默认false
     */
    boolean isFreeForVisitor() default false;

    /**
     * 是否对所有登陆的用户开放, 默认false
     */
    boolean isFreeForWorker() default false;

    /**
     * 系统级,仅管理员有权限
     */
    boolean isOnlyForSystem() default false;

    /**
     * 父节点CODE 格式为 ${controller}_${action} 如果上级是一级菜单 则是 PrimaryMenuController 类中的方法名
     * 多个以逗号隔开
     *
     */
    String parentButtonCode() default "";

    /**
     * 默认参数 只有菜单用到
     */
    String defaultParams() default "";

    /**
     * 旧code 根据旧code 更新
     */
    String oldCode() default "";
}
