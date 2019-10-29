package com.nzxpc.handler.mem.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nzxpc.handler.mem.core.entity.DbSaver;
import com.nzxpc.handler.util.ReflectUtil;

import javax.persistence.Entity;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ContainerChecker {
    private static <T extends ContainerBase> void makeSureMap(Class<T> dataContainerClass) {
        Set<String> set = Set.of("IdMap");
        for (Field item : dataContainerClass.getDeclaredFields()) {
            if (!Modifier.isStatic(item.getModifiers()) && !set.contains(item.getName())
                    && (Map.class.isAssignableFrom(item.getType()) && item.getType() != ConcurrentHashMap.class)) {
                System.out.println(String.format("%s.%s的类型必须得是ConcurrentHashMap", dataContainerClass.getSimpleName(), item.getName()));
                System.exit(0);
            }
        }
    }

    private static Set<DbSaverBase> dbSavers = new HashSet<>();
    private static final String FALL = ".fall";

    private static <T extends DbSaverBase> void newDbSavers(Class<T> dbSaverClass) {
        ReflectUtil.scanClasses(dbSaverClass.getPackageName(), null, clazz -> {
            try {
                Object o = clazz.getDeclaredConstructor().newInstance();
                if (!(o instanceof DbSaverBase)) {
                    throw new Exception(o.getClass().getName() + "必须要继承" + DbSaverBase.class.getSimpleName());
                }
                dbSavers.add((DbSaverBase) o);
            } catch (Throwable e) {
                System.exit(0);
            }
            return true;
        });
    }

    static <T1 extends ContainerBase, T2 extends DbSaverBase> void check(Class<T1> dataContainerClass, Class<T2> daSaverClass) {
        for (Field field : dataContainerClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Type type = field.getGenericType();
            if (type instanceof ParameterizedType) {
                ParameterizedType genericType = (ParameterizedType) type;
                for (Type t : genericType.getActualTypeArguments()) {
                    if (!(t instanceof ParameterizedType)) {
                        Annotation[] annotations = ((Class) t).getAnnotations();
                        if (Arrays.stream(annotations).anyMatch(a -> a.annotationType() == Entity.class)) {
                            if (field.getDeclaredAnnotation(DbSaver.class) == null) {
                                throw new RuntimeException(dataContainerClass.getSimpleName() + "的属性" + field.getName() + "缺少" + DbSaver.class.getSimpleName() + "注释");

                            }
                        }
                    }
                }
            }
        }
        makeSureMap(dataContainerClass);
        newDbSavers(daSaverClass);
    }

    static void fall() {
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss")) + FALL;
        Map<String, Collection> allData = new HashMap<>();
        try {
            System.out.println("生成落地数据文件" + fileName);
            for (DbSaverBase dbSaver : dbSavers) {
                Collection data = dbSaver.prepareData();
                if (data.size() > 0) {
                    allData.put(dbSaver.getDataClass().getName(), data);
                }

            }
            new ObjectMapper().findAndRegisterModules().writeValue(new File(fileName), allData);
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }
}
