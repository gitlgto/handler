package com.nzxpc.handler.util;

import com.nzxpc.handler.util.db.PageBean;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * 主要为了方便查询内存的map
 * 如果collection和pageBean的泛型不一致，一定要设置converter，否则会报类型转换错误
 */
public class PageUtil<E, R> {
    private Stream<E> stream;
    private Class clazz;
    private PageBean<R> pageBean;
    private boolean empty;
    private boolean defaultComparator;
    private LinkedList<Comparator<E>> comparators = new LinkedList<>();
    private LinkedList<Predicate<E>> skipers = new LinkedList<>();
    private Function<List<E>, List<R>> converter;
    private Map<String, SumModel> sumMap = new HashMap<>();

    @AllArgsConstructor
    private class SumModel<T> {
        private T value;
        private BiFunction<E, T, T> function;
    }

    private PageUtil(Collection<E> collection, PageBean<R> pageBean) {
        if (collection == null || collection.isEmpty()) {
            empty = true;
        } else {
            this.clazz = collection.iterator().next().getClass();
            this.stream = collection.stream();
        }
        this.pageBean = pageBean;
    }

    public static <E, R> PageUtil<E, R> create(Collection<E> collection, PageBean<R> pageBean) {
        return new PageUtil<>(collection, pageBean);
    }

    public PageUtil<E, R> filter(Predicate<E> filter) {
        if (!empty) {
            this.stream = stream.filter(filter);
        }
        return this;
    }

    /**
     * @param name     字段名称
     * @param value    默认值
     * @param function E 上一次的结果，返回本次结果
     * @param <T>
     * @return
     */
    public <T> PageUtil<E, R> sum(String name, T value, BiFunction<E, T, T> function) {
        sumMap.put(name, new SumModel<>(value, function));
        return this;
    }

    /**
     * 自定义排序，可以添加多个，按先后顺序执行
     */
    public PageUtil<E, R> sort(Comparator<E> comparator) {
        if (!empty && comparator != null) {
            this.comparators.add(comparator);
        }
        return this;
    }

    /**
     * 只能生效一次?
     *
     * @return
     */
    public PageUtil<E, R> sort() {
        if (!empty && !defaultComparator) {
            defaultComparator = true;
            Comparator<E> comparator = getComparator();
            if (comparator != null) {
                this.comparators.add(comparator);
            }
        }
        return this;
    }

    /**
     * 结果集合转换，对于两个泛型不一致进行转换
     */
    public PageUtil<E, R> convert(Function<List<E>, List<R>> converter) {
        this.converter = converter;
        return this;
    }

    /**
     * 查询过滤，汇总不过滤
     */
    public PageUtil<E, R> skip(Predicate<E> skiper) {
        this.skipers.add(skiper);
        return this;
    }

    /**
     * 分页
     */
    public void page() {
        //跳到第几页 一页记录数  3页 20条 40
        int skip = (pageBean.getPageNo() - 1) * pageBean.getPageSize();
        int limit = pageBean.getPageSize();
        page(skip, limit);
    }

    public void page(int skip, int limit) {
        if (!empty) {
            //数组长度是1，最大下标是0
            int[] count = new int[1];
            //排序
            if (!comparators.isEmpty()) {
                for (Comparator<E> comparator : comparators) {
                    stream = stream.sorted(comparator);
                }
            }
            //汇总 s字段名称
            stream = stream.peek(e -> {
                for (String s : sumMap.keySet()) {
                    SumModel sumModel = sumMap.get(s);
                    sumModel.value = sumModel.function.apply(e, sumModel.value);
                }
                ++count[0];
            });
            if (!skipers.isEmpty()) {
                for (Predicate<E> predicate : skipers) {
                    stream = stream.filter(predicate);
                }
            }
            List<R> collect = stream.skip(skip).collect(new LimitToList<>(limit, converter));
            pageBean.setData(collect);
            pageBean.setRowCount(count[0]);
        } else {
            pageBean.setData(new ArrayList<>());
        }
        //统计
        Map<String, Object> map = pageBean.getSumMap();
        for (String s : sumMap.keySet()) {
            SumModel sumModel = sumMap.get(s);
            map.put(s, sumModel.value);
        }

    }

    static class LimitToList<T, R> implements Collector<T, List<T>, List<R>> {

        private int limit = 0;
        private boolean finished;
        private Function<List<T>, List<R>> converter;

        LimitToList(int limit, Function<List<T>, List<R>> converter) {
            this.limit = limit;
            this.converter = converter;
        }

        //创建一个累加器，对应list
        @Override
        public Supplier<List<T>> supplier() {
            return ArrayList::new;
        }

        //把一个对象添加到累加器中，对应list添加
        @Override
        public BiConsumer<List<T>, T> accumulator() {
            return (list, item) -> {
                if (!finished) {
                    list.add(item);
                    --limit;
                    finished = limit == 0;
                }
            };
        }

        //把一个累加器和另一个累加器合并起来，对应合并两个list，返回list
        @Override
        public BinaryOperator<List<T>> combiner() {
            return (left, right) -> {
                left.addAll(right);
                return left;
            };
        }

        //把a转换成r
        @Override
        public Function<List<T>, List<R>> finisher() {
            if (converter == null) {
                return (t) -> (List<R>) t;
            }
            return this.converter;
        }

        //将会以并行的方式进行操作
        @Override
        public Set<Characteristics> characteristics() {
            return Collections.unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT));
        }
    }


    private Comparator<E> getComparator() {
        Field field = null;
        if (StringUtils.isBlank(pageBean.getOrderField())) {
            Field createAt = ReflectUtil.getField(clazz, "createAt");
            if (createAt == null) {
                createAt = ReflectUtil.getField(clazz, "id");
            }
            field = createAt;
        } else {
            field = ReflectUtil.getField(clazz, pageBean.getOrderField());
        }
        if (field == null) {
            return null;
        }
        Comparator<E> comparator;
        //设置Field对象的Accessible的访问标志位为ture，就可以通过反射获取私有变量的值，在访问时会忽略访问修饰符的检查
        field.setAccessible(true);
        //返回该字段的的字段类型（int，long，string）
        Class<?> clazz = field.getType();
        //判断是否为原始类型（char,boolean,byte,int,long） 判断是否为某个类的父类
        if (clazz.isPrimitive() || Comparable.class.isAssignableFrom(clazz)) {
            Field finalFiled = field;
            comparator = (t, t1) -> {
                try {
                    //因为继承，向上引用转换
                    //这种方式就是可以获取当前实体对象t或者t1，对应t或者t1中filed（id,createAt）的value值
                    Comparable c1 = (Comparable) finalFiled.get(t);
                    Comparable c2 = (Comparable) finalFiled.get(t1);
                    //然后进行比较
                    if (c1 == null) {
                        return -1;
                    }
                    if (c2 == null) {
                        return 1;
                    }
                    return c1.compareTo(c2);
                } catch (IllegalAccessException e) {
                    LogUtil.err("排序异常", e);
                }
                return 0;
            };
        } else {
            LogUtil.err("排序异常: 类[" + clazz.getName() + "]中的属性 " + pageBean.getOrderField() + " 不是基本类型并且没有实现Comparable接口, 不能用于排序");
            return null;
        }
        if (StringUtils.equalsIgnoreCase(pageBean.getOrderDirection(), "desc")) {
            return comparator.reversed();
        } else {
            return comparator;
        }
    }
}
