package com.nzxpc.handler.util.db.migrate;

import com.nzxpc.handler.util.db.migrate.core.MigrateCore;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 数据库迁移工具类
 * 新增类->创建表
 * 修改属性名，修改表名
 * 新增属性，添加到对应的表
 * 删除属性，删除对应列
 * 修改索引
 * 修改属性或约束
 * 总结出现的几点问题。1.sql语句的书写问题，其次字段的使用错误，反射应当获取直至父类
 */
public class DbMigrateUtil {
    public static void migrate(String url, String username, String password, Class... packageClass) {
        MigrateCore core = new MigrateCore(url, username, password, Arrays.stream(packageClass).map(Class::getPackageName).collect(Collectors.toSet()));
        core.start();
    }

    public static void migrate(String url, String username, String password, String... packageClass) {
        MigrateCore core = new MigrateCore(url, username, password, Arrays.stream(packageClass).collect(Collectors.toSet()));
        core.start();
    }
}
