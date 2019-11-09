package com.nzxpc.handler.util;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.internal.util.ReflectHelper;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public class CollectionUtil {
    /**
     * 将From类型的List复制成To类型的List
     * 复制list,忽略值为null的属性<br>
     * 如果sourceList为空或者T没有无参构造（生成不了无参实体对象）,则返回空列表
     * 1.如果没有无参构造，只能生成有参构造的实体对象。2.另外默认有个无参构造，如果写有有参构造会覆盖
     * 无参构造，得另外加有参构造。
     * 无参构造运行是需要往上一级一级找无参，甚至找到object
     * 如果其中一级只有有参，没有无参，会发生错误。但只有无参，没有有参一定不会出错。
     * 子类继承父类的时候会自动继承父类的默认构造函数（也就是继承那个无参数的构造函数）。
     * 而子类类里面已经有一个带有参数的构造函数了，如果没有写那个默认的不带参数的构造函数的话，继承的时候子类就会报错，因为系统不知道要不继承哪个构造函数，
     * 必须明确的使用super（）关键字来描述。所以一般为了避免这种错误的发生，在有带有多个构造函数的类里面都会写一个不带参数的构造函数。
     * 如果父类没有无参构造，A:在父类中加一个无参构造方法
     * B:通过使用super关键字去显示的调用父类的带参构造方法，且必须放在第一条
     * C:子类通过this去调用本类的其他构造方法 类中一定要有一个去访问了父类的构造方法，否则父类数据就没有初始化。
     * 子类实例化时，默认调用父类的无参构造方法（不管子类的构造器有没有参数，因为子类继承的是父类的属性和方法，
     * 只调用父类的无参构造器就可以继承父类的属性和方法，因此不会调用父类的有参构造器），再调用子类的有参/无参构造器
     * 父类没有无参的构造方法，而有有参数的构造方法，那么子类继承的时候，就不能自定义无参的构造方法或者有参的构造
     * （因为在子类构造方法中，第一行是默认调用父类的的无参构造，此时是没有的，所以在子类中会报错。即使不写构造方法，还是会默认调用父类的
     * 无参构造，而此时父类无参构造是没有的，所以子类会报错）。3.所以在父类没有无参构造时，子类不能写无参构造了，只能写有参构造，并传入参数调用父类有参构造
     * 进行初始化父类数据，否则会报错（父类无无参构造），子类的所有构造函数均要手动掉super。（当然可以调用父类有参构造，第一行）。
     * 4.反之，父类有无参构造方法，子类若定义了的无参构造方法，在这个无参数的构造方法里是等于默认第一行super()，有参构造也会默认调用。
     *
     * @param sourceList  操作list
     * @param targetClazz 目标实体
     * @return
     */
    public static <S, T> List<T> copy(List<S> sourceList, Class<T> targetClazz) {
        List<T> list = new ArrayList<>();
        if (sourceList == null || sourceList.size() < 1) {
            return list;
        }
        Assert.notNull(targetClazz, "TargetClazz must not be null");
        //如果无无参构造，抛出异常并返回list 私有属性只能在本类使用，如果是反射，则setAccessible（true）暴力获取属性值
        //throwable和exception的区别：1、throwable是父类，exception是子类。2、throwable是根基，exception是从throwable派生出来的。
        //3、throwable中包括exception（异常）和error（错误）4、throwable用来定义所有可以作为异常被抛出来的类，exception专指程序本身可以处理的异常，一般性的异常。
        //在Java程序中，所有异常对象的根基类是Throwable，Throwable从Object直接继承而来（这是Java系统所强制要求的）。
        // Throwable有两个重要的子类：Exception（异常）和 Error（错误），二者都是 Java 异常处理的重要子类，各自都包含大量子类。
        //Error（错误）是程序无法处理的错误，表示运行应用程序中较严重问题。大多数错误与代码编写者执行的操作无关，
        //而表示代码运行时 JVM（Java 虚拟机）出现的问题。Exception（异常）是程序本身可以处理的异常。
        //Error是一种严重的问题，应用程序不应该捕捉它。 Exception一般可能是程序和业务上的错误，是可以恢复的。
        try {
            targetClazz.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            LogUtil.err(CollectionUtil.class, e);
            return list;
        }
        Class<?> sourceClazz = sourceList.get(0).getClass();
        HashMap<PropertyDescriptor, PropertyDescriptor> map = MyBeanUtils.getPropertyDescriptors(sourceClazz, targetClazz);

        sourceList.forEach(source -> {
            try {
                //放在外面始终就是那一个，不会新生成对象，所有添加的model都属于同一个指向同一个地址，
                // 在修改时会找对应对象地址修改所有，且值都相等为最后一个赋的值，并且set对待地址一样的也只存一个
                //放在里面每次遍历都会生成一个并赋值，指向不同的地址。

                T t = targetClazz.getDeclaredConstructor().newInstance();
                MyBeanUtils.copy(source, t, map, true);
                list.add(t);

                //直接这样写，每次都得获取class，效率会低。
//                T t = MyBeanUtils.copy(source, targetClazz);
//                list.add(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
        return list;
    }

    /**
     * 排序
     *
     * @param data           数据
     * @param orderDirection 排序方向 desc降序 asc升序
     * @param orderField     排序字段
     * @param <T>            数据类型
     * @return
     */
    public static <T> List<T> sort(List<T> data, String orderDirection, String orderField) {
        if (data == null || StringUtils.isBlank(orderDirection) || StringUtils.isBlank(orderField)) {
            return data;
        }
        if (!StringUtils.equals("desc", orderDirection) && !StringUtils.equals("asc", orderDirection)) {
            throw new RuntimeException("排序方向" + orderDirection + "非法");
        }
        try {
            Class<?> clazz = data.get(0).getClass();
            //找到clazz中对应orderfield字段get方法(不用考虑私有属性)
            final Method getter = ReflectHelper.findGetterMethod(clazz, orderField);

            //1field.get(b1)或者get方法反射获取都可以
//            Field field = ReflectUtil.getField(clazz, orderField);
//            field.setAccessible(true);
//            field.getType();
//            field.get();
//1

            //获取该字段对应get方法返回数据类型
            Class<?> type = getter.getReturnType();
            //判断该类型是否为原始类型或者实现Comparable接口，这样的类型才能进行比较大小
            //1
            Comparator<T> comparator;
            //1
            if (Comparable.class.isAssignableFrom(type) || type.isPrimitive()) {
               //1
                comparator = (b1, b2) -> {
                    try {
                        //field.get(b1)或者get方法反射获取都可以
                        Comparable ca = (Comparable) getter.invoke(b1);
                        Comparable cb = (Comparable) getter.invoke(b2);
                        if (ca == null) {
                            return -1;
                        }
                        if (cb == null) {
                            return 1;
                        }
                        return ca.compareTo(cb);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
                if (StringUtils.equals("desc", orderDirection)) {
                    Comparator<T> reversed = comparator.reversed();
                    data.sort(reversed);
                } else {
                    data.sort(comparator);
                }
                //1

                //2
//                data.sort((c1, c2) -> {
//                    //反射getOrderField方法获取实体c1的orderField的值
//                    try {
//                        Comparable ca = (Comparable) getter.invoke(c1);
//                        Comparable cb = (Comparable) getter.invoke(c2);
//                        //要不要考虑值为null
//
//                        return StringUtils.equals(orderDirection, "asc") ? ca.compareTo(cb) : cb.compareTo(ca);
//                    } catch (Throwable e) {
//                        throw new RuntimeException(e);
//                    }
//                });
                //2
            } else {
                throw new RuntimeException(orderField + "不支持排序");
            }
        } catch (Exception e) {
            LogUtil.err(CollectionUtil.class, e);
        }
        return data;
    }
}
