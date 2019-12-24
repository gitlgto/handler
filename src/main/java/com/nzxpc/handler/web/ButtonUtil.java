package com.nzxpc.handler.web;

import com.google.common.collect.ImmutableMap;
import com.nzxpc.handler.util.LogUtil;
import com.nzxpc.handler.util.db.DbUtil;
import com.nzxpc.handler.util.db.SqlHelper;
import com.nzxpc.handler.web.entity.Button;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ButtonUtil {

    private static HashMap<String, Button> CodeButtonMap = new HashMap<>();
    private static HashMap<String, Button> OldCodeButtonMap = new HashMap<>();
    private static HashMap<String, Button> DbCodeButtonMap = new HashMap<>();

    private static void loadButtonMap() {
        DbCodeButtonMap.clear();
        DbUtil.getSqlHelper(Button.class).list().forEach(item -> {
            DbCodeButtonMap.put(item.getCode(), item);
        });
    }

    private static void clear() {
        CodeButtonMap.clear();
        OldCodeButtonMap.clear();
        DbCodeButtonMap.clear();
    }

    public static void init(Class<?>... entityClass) {
        loadButtonMap();
        loadSystemButtons(entityClass);

        HashSet<Integer> tempIds = new HashSet<>();
        HashSet<String> tempCodes = new HashSet<>();
        List<Button> tempButton = new ArrayList<>();

        SqlHelper<Button> sqlHelper = DbUtil.getSqlHelper(Button.class);
        //加载系统的button，带oldCode
        OldCodeButtonMap.forEach((code, item) -> {
            //加载数据库button
            Button button = DbCodeButtonMap.remove(code);
            if (button != null && !isEquals(item, button)) {
                tempButton.add(button);
            }
        });
        if (tempButton.size() > 0) {
            tempButton.forEach(item -> {
                sqlHelper.updateById(item, "code", "name", "uri", "menu", "parentCode", "freeForWorker", "freeForVisitor", "onlyForSystem", "orderNum", "imgClass");
                DbCodeButtonMap.put(item.getCode(), item);
            });
        }
        tempButton.clear();

        // 删除多余的
        DbCodeButtonMap.forEach((code, item) -> {
            if (!CodeButtonMap.containsKey(code)) {
                tempIds.add(item.getId());
                tempCodes.add(code);
            }
        });
        DbCodeButtonMap.keySet().removeAll(tempCodes);
        tempCodes.clear();

        if (tempIds.size() > 0) {
            sqlHelper.doSql("DELETE FROM `Button` WHERE `id` IN (:ids)", ImmutableMap.of("ids", tempIds));
        }
        tempIds.clear();

        // 把待添加的过滤出来
        CodeButtonMap.forEach((code, item) -> {
            if (!DbCodeButtonMap.containsKey(code)) {
                tempButton.add(item); // 直接加进去
                tempCodes.add(code);
            }
        });
        CodeButtonMap.keySet().removeAll(tempCodes);
        tempCodes.clear();
        if (tempButton.size() > 0) {
            sqlHelper.addBatch(tempButton);
        }
        tempButton.clear();

        // 更新(如果需要更新)
        CodeButtonMap.forEach((code, item) -> {
            Button button = DbCodeButtonMap.get(code);
            if (!isEquals(item, button)) {
                tempButton.add(button);
            }
        });

        if (tempButton.size() > 0) {
            tempButton.forEach(item -> {
                sqlHelper.updateById(item, "name", "uri", "menu", "parentCode", "freeForWorker", "freeForVisitor", "onlyForSystem", "orderNum","imgClass");
            });
        }

        tempButton.clear();
        clear();

    }

    private static boolean isEquals(Button source, Button target) {
        boolean isEquals = org.apache.commons.lang.StringUtils.equals(source.getCode(), target.getCode())
                && org.apache.commons.lang.StringUtils.equals(source.getUri(), target.getUri())
                && org.apache.commons.lang.StringUtils.equals(source.getParentCode(), target.getParentCode())
                && org.apache.commons.lang.StringUtils.equals(source.getName(), target.getName())
                && source.isMenu() == target.isMenu()
                && source.isOnlyForSystem() == target.isOnlyForSystem()
                && source.isFreeForVisitor() == target.isFreeForVisitor()
                && source.isFreeForWorker() == target.isFreeForWorker()
                && source.getOrderNum() == target.getOrderNum()
                && org.apache.commons.lang.StringUtils.equals(source.getImgClass(), target.getImgClass());
        if (!isEquals) {
            target.setCode(source.getCode())
                    .setUri(source.getUri())
                    .setParentCode(source.getParentCode())
                    .setName(source.getName())
                    .setMenu(source.isMenu())
                    .setOnlyForSystem(source.isOnlyForSystem())
                    .setFreeForVisitor(source.isFreeForVisitor())
                    .setFreeForWorker(source.isFreeForWorker())
                    .setOrderNum(source.getOrderNum())
                    .setImgClass(source.getImgClass());
        }
        return isEquals;
    }

    private static void loadSystemButtons(Class<?>... entityClass) {
        String[] arr = new String[entityClass.length];
        int i = 0;
        for (Class<?> clazz : entityClass) {
            arr[i++] = clazz.getPackageName();
        }
        //找实现controllerInterface的接口的类
        List<Class<?>> beanClasses = scanPackage(new AssignableTypeFilter(ControllerInterface.class), arr);
        for (Class<?> beanClass : beanClasses) {
            String ctlUrl = StringUtils.uncapitalize(beanClass.getSimpleName().replace("Controller", ""));
            Method[] methods = beanClass.getMethods();
            for (Method method : methods) {
                int md = method.getModifiers();
                if (Modifier.isPublic(md) && method.isAnnotationPresent(ButtonAttribute.class) && !Modifier.isStatic(md)) {
                    String actUrl = "";
                    if (method.isAnnotationPresent(GetMapping.class) || method.isAnnotationPresent(PostMapping.class) || method.isAnnotationPresent(RequestMapping.class)) {
                        actUrl = org.apache.commons.lang.StringUtils.uncapitalize(method.getName());
                    }
                    ButtonAttribute attribute = method.getAnnotation(ButtonAttribute.class);
                    String parentButtonCode = attribute.parentButtonCode();
                    Button button = new Button();
                    button.setParentCode(parentButtonCode);
                    if (StringUtils.isNotBlank(ctlUrl) && StringUtils.isNotBlank(actUrl)) {
                        button.setUri(String.format("/%s/%s", ctlUrl, actUrl));
                        button.setCode(String.format("%s_%s", ctlUrl, actUrl));
                    } else {
                        button.setUri("");
                        button.setCode(method.getName());
                    }
                    button.setDefaultParams(attribute.defaultParams());
                    button.setName(attribute.name());
                    button.setMenu(attribute.isMenu());
                    button.setImgClass(attribute.icon());
                    button.setOrderNum(attribute.orderNum());
                    button.setFreeForVisitor(attribute.isFreeForVisitor());
                    button.setFreeForWorker(attribute.isFreeForWorker());
                    button.setOnlyForSystem(attribute.isOnlyForSystem());
                    CodeButtonMap.put(button.getCode(), button);
                    if (StringUtils.isNotBlank(attribute.oldCode())) {
                        OldCodeButtonMap.put(attribute.oldCode(), button);
                    }

                }
            }

        }
    }

    /**
     * 获取类
     */
    private static List<Class<?>> scanPackage(String packageName, TypeFilter includeFilter) {
        List<Class<?>> ret = new ArrayList<>();
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
            scanner.addIncludeFilter(includeFilter);
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(packageName)) {
                Class<?> entity = Class.forName(beanDefinition.getBeanClassName());
                ret.add(entity);
            }
        } catch (Exception e) {
            LogUtil.err(e);
        }
        return ret;
    }

    public static List<Class<?>> scanPackage(TypeFilter includeFilter, String... packageNames) {
        List<Class<?>> ret = new ArrayList<>();
        for (String packageName : packageNames) {
            ret.addAll(scanPackage(packageName, includeFilter));
        }
        return ret;
    }
}
