package com.nzxpc.handler.util.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public abstract class SqlHelper<T> extends NamedParameterJdbcDaoSupport {
    private final static char QuoteMark = '`';
    List<String> propList = new ArrayList<>();
    String tableName;
    Class<T> persistentClass;


    private static String conditionSql(Object inputValue, boolean isIgnore, Map<String, Object> argMap, String srcSql, String conditionSql) {
        String ret;
        if (inputValue == null || isIgnore) {
            ret = srcSql;
        } else {
            ret = conditionSql(srcSql, conditionSql);
            Matcher matcher = Pattern.compile(":(\\w+)").matcher(conditionSql);
            if (matcher.find()) {
                String group = matcher.group(1);
                if (inputValue instanceof Enum<?>) {
                    Enum<?> value = (Enum<?>) inputValue;
                    argMap.put(group, value.ordinal());
                } else {
                    argMap.put(group, inputValue);
                }
            }
        }
        return ret;
    }

    protected NamedParameterJdbcTemplate getJt() {
        return getNamedParameterJdbcTemplate();
    }

    /**
     * 应用场景，返回值是基本数据类型
     * @param sql
     * @param map
     * @param primitiveTypeClass
     * @param <PrimitiveType>
     * @return
     */
    public <PrimitiveType> List<PrimitiveType> simpleList(String sql, Map<String, Object> map, Class<PrimitiveType> primitiveTypeClass) {
        return getJt().queryForList(sql, map, primitiveTypeClass);
    }

    /**
     * 返回值中的sql示例：set name=:name,money=:money
     *
     */
    protected abstract PrepareSqlParamResult prepareSqlParamForUpdate(Object bean, List<String> updateColumns);

    /**
     * 返回的sql示例：(name,money) values (:name,:money)
     *
     */
    protected abstract PrepareSqlParamResult prepareSqlParamForAdd(Object bean);

    public abstract boolean exist(String conditionSql, Map<String, Object> argMap);


    /**
     * 将map中的参数枚举转换成ordinal
     */

    static Map<String, Object> changeEum(Map<String, Object> argMap) {
        Map<String, Object> ret = null;
        if (argMap != null) {
            ret = new HashMap<>();
            for (String s : argMap.keySet()) {
                Object o = argMap.get(s);
                if (o.getClass().isEnum()) {
                    Enum o1 = (Enum) o;
                    int ordinal = o1.ordinal();
                    ret.put(s, ordinal);
                } else {
                    ret.put(s, o);
                }
            }
        }
        return ret;
    }

    /**
     * 执行原生sql进行db修改
     */
    public int doSql(String sql, Map<String, Object> argMap) {
        argMap = changeEum(argMap);
        return getJt().update(sql, argMap);
    }

    /**
     * 执行自定义数据绑定的查询，无分页
     * 没有数据则返回空列表
     * rawtypes 传参也要传带泛型的参数
     */
    public <E> List<E> query(String sql, Map<String, Object> map, Class<E> clazz) {
        @SuppressWarnings({"unchecked", "rawtypes"}) List<E> list = getJt().query(sql, map, new BeanPropertyRowMapper(clazz));
        return list;
    }

    /**
     * 执行自定义数据绑定的查询，带分页
     */
    public abstract <E> PageBean<E> query(PageBean<E> pageBean, String sql, Map<String, Object> map, Class<E> clazz);

    /**
     * 按传入的条件更新，针对那些直接继承自EntityBase的实现，{@link com.nzxpc.handler.mem.core.entity.EntityBase}
     * 返回影响的行数
     */
    public abstract <Entity> int update(Entity data, String conditionSql, Map<String, Object> conditionMap, String... updateColumns);

    /**
     * 按id更新，针对直接继承IdEntity的实体，{@link com.nzxpc.handler.mem.core.entity.IdEntityBasePure}
     * 返回影响的行数
     */
    @SuppressWarnings("ALL")
    public abstract <IdEntity extends IdEntityBasePure> int updateById(IdEntity data, String... updateColumns);

    public abstract <IdEntity extends IdEntityBasePure, V> int updateById(IdEntity data, com.nzxpc.handler.mem.core.entity.Getter<IdEntity, V>... getters);

    /**
     * 按主键id删除
     */
    public abstract int deleteById(int id);

    /**
     * 条件删除
     * Map<String, Object> param = ImmutableMap.of("ids", Lists.newArrayList(2, 3, 4), "money", 0);
     * userDao.delete("id in (:ids) and money>=:money", param);
     */
    public abstract int delete(String conditionSql, Map<String, Object> conditionArgMap);

    /**
     * 通过id获取数据
     */
    public abstract T getById(int id);

    /**
     * 为了更新而获取，会加上行级锁，调用此方法需要开启事务
     */
    public abstract T getByIdForUpdate(int id);

    /**
     * 查找第一条数据，返回第一个被添加进去的数据
     */
    public abstract T get();

    /**
     * 查找第一条数据，返回第一个被添加进去的记录
     * 锁住数据直到事务提交
     */
    public abstract T getForUpdate();

    public abstract T getForUpdate(String conditionSql, Map<String, Object> argMap);

    /**
     * 按指定条件查找第一条数据
     */
    public abstract T get(String conditionSql, Map<String, Object> argMap);

    /**
     * 添加数据，针对主键为id的数据，返回的id会赋值到实体中
     */
    public abstract <IdEntity extends IdEntityBasePure> void add(IdEntity t);

    /**
     * 批量添加数据，注意sql连接串上需要打开设置：rewriteBatchedStatements=true，否则不起作用
     */
    public abstract <Entity> int addBatch(Collection<Entity> entities);

    /**
     * 列出所有记录，无分页
     */
    public abstract List<T> list();

    /**
     * 检索所有记录，无分页
     */
    public abstract List<T> list(String sql, Map<String, Object> argMap);

    /**
     * 添加数据，针对主键不是id，一般用于添加中间表
     */
    public abstract <Entity> void add(Entity t);

    /**
     * desc 将查询到的单行数据，以map的形式返回，比如"select sum(money) as totalMoney from table1"，
     * 返回的Map键值为totalMoney,值为对应的数值。若查询的结果集为空，则Map的size为0，若不只一条记录，
     * 则会抛异常，比如"select name,age from table1"，所以一定要确保返回的结果集只有一行
     **/
    public Map<String, Object> getSimple(String sql, Map<String, Object> argMap) {
        //将map中参数枚举转成ordinal
        argMap = changeEum(argMap);
        Map<String, Object> ret;
        try {
            ret = getJt().queryForMap(sql, argMap);
        } catch (EmptyResultDataAccessException e) {
            ret = new HashMap<String, Object>();
        }
        return ret;
    }

    public abstract int getCount(String sql, Map<String, Object> argMap);

    /**
     * 分页查询
     */
    public abstract PageBean<T> list(PageBean<T> pageBean, String sql, Map<String, Object> argMap);

    /**
     * desc 分页查询
     */
    public abstract PageBean<T> list(PageBean<T> pageBean, String sql);

    /**
     * 给字段两边加上`
     */
    String parseName(String name) {
        return QuoteMark + name + QuoteMark;
    }

    /**
     * 必须是查单列的sql语句
     * 参数可以为null
     */
    public abstract List<T> simpleList(String sql, Map<String, Object> argMap);

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    protected static class PrepareSqlParamResult {
        protected String sql;
        protected MapSqlParameterSource map;
    }

    private static String conditionSql(String srcSql, String conditionSql) {
        String ret;
        if (StringUtils.containsIgnoreCase(srcSql, "where")) {
            ret = srcSql + "and" + conditionSql;
        } else {
            if (StringUtils.containsIgnoreCase(conditionSql, "where")) {
                ret = srcSql + "" + conditionSql;
            } else {
                ret = srcSql + "where" + conditionSql;
            }
        }
        return ret;
    }
}
