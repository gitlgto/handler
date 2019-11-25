package com.nzxpc.handler.util.db;

import com.google.common.collect.ImmutableMap;
import com.nzxpc.handler.mem.core.entity.Getter;
import com.nzxpc.handler.util.ReflectUtil;
import com.nzxpc.handler.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.validation.constraints.Max;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

@SuppressWarnings("unchecked")
public class MySqlHelper<T> extends SqlHelper<T> {

    public MySqlHelper(Class<T> persistentClass, JdbcTemplate jt) {
        tableName = persistentClass.getSimpleName();
        this.persistentClass = persistentClass;
        propList.addAll(getPropList(persistentClass));
        setJdbcTemplate(jt);
    }

    protected MySqlHelper() {
    }

    /**
     * 获取属性名,实体（Class）中所有的字段名,不包括class
     *
     * @return 属性名列表
     */
    protected List<String> getPropList(Class<?> clazz) {
        List<String> propList = new ArrayList<>();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            if (!StringUtils.equals("class", descriptor.getName())) {
                propList.add(descriptor.getName());
            }
        }
        return propList;
    }

    /**
     * 返回值中的sql示例：set name=:name,money=:money
     * prepareSqlParamResult:
     * protected String sql;
     * protected MapSqlParameterSource map;
     * updateColumns 要更新的列字段 bean要更新的实体 Proplist存储传入实体的所有属性字段 bean中字段是有值的 反射获取的map最终赋到MySQLmap
     * 反射获取bean中字段和值 根据传入的实体和list（需要获取的字段name） 根据class获取所有filed，每遍历一次filed，都执行一次传入的函数体（执行方法，lanmda表达式）
     * 使用该方法 bean应该已经赋给了值
     */
    @Override
    protected PrepareSqlParamResult prepareSqlParamForUpdate(Object bean, List<String> updateColumns) {
        PrepareSqlParamResult result = new PrepareSqlParamResult();
        StringJoiner sj = new StringJoiner(",");
        List<String> tempPropList = new ArrayList<>();
        for (String column : updateColumns) {
            Optional<String> propName = propList.stream().filter(a -> StringUtils.containsIgnoreCase(a, column)).findFirst();
            if (propName.isPresent()) {
                tempPropList.add(propName.get());
            }
        }
        Map<String, Object> fieldMap = ReflectUtil.getAllFields(bean, tempPropList);
        for (String column : updateColumns) {
            Optional<String> propName = propList.stream().filter(a -> StringUtils.equalsIgnoreCase(a, column)).findFirst();
            if (fieldMap.containsKey(propName.get())) {
                sj.add(parseName(propName.get()) + "=:" + propName.get());
            } else {
                sj.add(parseName(propName.get()) + "=NULL");
            }
        }
        result.sql = "SET" + sj.toString();
        result.map = new MapSqlParameterSource(fieldMap);
        return result;
    }

    /**
     * 返回的sql示例：(name,money) values (:name,:money)
     */
    @Override
    protected PrepareSqlParamResult prepareSqlParamForAdd(Object bean) {
        PrepareSqlParamResult ret = new PrepareSqlParamResult();
        Map<String, Object> fieldMap = ReflectUtil.getAllFields(bean);
        StringJoiner sjName = new StringJoiner(",", "(", ")");
        StringJoiner sjVal = new StringJoiner(",", "(", ")");
        for (String propName : fieldMap.keySet()) {
            sjName.add(parseName(propName));
            sjVal.add(":" + propName);
        }
        ret.sql = sjName.toString() + " VALUES " + sjVal.toString();
        ret.map = new MapSqlParameterSource(fieldMap);
        return ret;
    }

    /**
     * 不忽略任何值
     *
     * @param bean
     * @return
     */
    protected PrepareSqlParamResult prepareSqlParamForBatchAdd(Object bean) {
        PrepareSqlParamResult ret = new PrepareSqlParamResult();
        Map<String, Object> map = ReflectUtil.getAllFieldsForBatchAdd(bean);
        StringJoiner sjName = new StringJoiner(",", "(", ")");
        StringJoiner sjVal = new StringJoiner(",", "(", ")");
        for (String propName : map.keySet()) {
            sjName.add(parseName(propName));
            sjVal.add(":" + propName);
        }
        ret.sql = sjName.toString() + " VALUES " + sjVal.toString();
        ret.map = new MapSqlParameterSource(map);
        return ret;
    }

    /**
     * 指定数组size，超过越界，存入对应index的数据
     */
    protected <Entity> SqlParameterSource[] prepareSqlParams(Collection<Entity> collection) {
        SqlParameterSource[] sources = new SqlParameterSource[collection.size()];
        int i = 0;
        for (Entity bean : collection) {
            Map<String, Object> map = ReflectUtil.getAllFieldsForBatchAdd(bean);
            sources[i++] = new MapSqlParameterSource(map);
        }
        return sources;
    }

    /**
     * 查询返回字段和查询结果对应map
     *
     * @param conditionSql 拼接sql name=：name
     * @param argMap       存入对应参数
     * @return
     */
    @Override
    public boolean exist(String conditionSql, Map<String, Object> argMap) {
        if (argMap == null) {
            throw new RuntimeException("argMap不能为空");
        }
        Map<String, Object> simple = getSimple("SELECT COUNT(0) `cnt` FROM " + parseName(tableName) + " WHERE " + conditionSql, argMap);
        return (long) simple.get("cnt") > 0;
    }

    @Override
    public <E> PageBean<E> query(PageBean<E> pageBean, String sql, Map<String, Object> argMap, Class<E> clazz) {
        argMap = changeEum(argMap);
        List<String> propList = getPropList(clazz); // 要使用自定义的类属性判断

        int startRow = pageBean.getStartRow();
        int offset = pageBean.getPageSize();
        StringBuilder sb = new StringBuilder(sql);
        int count = this.getCount(sb.toString(), argMap);
        pageBean.setRowCount(count);

        if (StringUtils.isNotBlank(pageBean.getOrderField()) && propList.contains(pageBean.getOrderField().toLowerCase())) {
            sb.append(" ORDER BY ");
            sb.append(parseName(pageBean.getOrderField()));
            if (StringUtils.isNotBlank(pageBean.getOrderDirection())) {
                sb.append(" ");
                sb.append(pageBean.getOrderDirection());
            }
        }
        sb.append(" LIMIT ");
        sb.append(startRow);
        sb.append(",");
        sb.append(offset);
        @SuppressWarnings({"unchecked", "rawtypes"}) List<E> data = getJt().query(sb.toString(), argMap, new BeanPropertyRowMapper(clazz));
        pageBean.setData(data);
        return pageBean;
    }

    @SuppressWarnings("ALL")
    @Override
    public <IdEntity extends IdEntityBasePure> int updateById(IdEntity data, String... updateColumns) {
        Map<String, Object> argMap = ImmutableMap.of("id", data.getId());
        return update(data, "`id`=:id", argMap, updateColumns);
    }

    @Override
    public <Entity> int update(Entity data, String conditionSql, Map<String, Object> conditionMap, String... updateColumns) {
        conditionMap = changeEum(conditionMap);
        for (String s : conditionMap.keySet()) {
            for (String s1 : updateColumns) {
                if (StringUtils.equalsIgnoreCase(s, s1)) {
                    throw new RuntimeException("更新列" + s1 + "和sql参数" + s + "一样");
                }
            }
        }
        //此步操作的目的是把传入的实体中数据取出，将要更新的字段和数据对应，存到map中返回，并返回sql。以供使用
        PrepareSqlParamResult result = prepareSqlParamForUpdate(data, Arrays.asList(updateColumns));
        result.map.addValues(conditionMap);
        String sql = " UPDATE " + parseName(tableName) + " " + result.sql + " WHERE " + conditionSql;
        return getJt().update(sql, result.map);
    }

    @Override
    public <IdEntity extends IdEntityBasePure, V> int updateById(IdEntity data, Getter<IdEntity, V>... getters) {
        String[] arr = new String[getters.length];
        for (int i = 0; i < getters.length; i++) {
            Getter<IdEntity, V> getter = getters[i];
            //获取传入的方法对应的字段名
            String prop = Util.prop(getter);
            arr[i] = prop;
        }
        //传入操作的实体，和要修改的字段，一般这种更新，都是先给要修改的实体中的字段赋值，
        // 然后传入该实体和要修改的字段（列），其中带sql的，则sql中的字段不能和要修改字段重复（列）。
        return updateById(data, arr);
    }

    @Override
    public int deleteById(int id) {
        return delete("id=:id", ImmutableMap.of("id", id));
    }

    @Override
    public int delete(String conditionSql, Map<String, Object> conditionArgMap) {
        conditionArgMap = changeEum(conditionArgMap);
        String sql = "DELETE FROM " + parseName(tableName) + " WHERE " + conditionSql;
        return getJt().update(sql, conditionArgMap);
    }

    @Override
    public T getById(int id) {
        return get("SELECT * FROM " + parseName(tableName) + " WHERE `id`=:id", ImmutableMap.of("id", id));
    }

    /**
     * 为了更新而获取，会加上行级锁，需要开启事务
     *
     * @param id
     * @return
     */
    @Override
    public T getByIdForUpdate(int id) {
        return get("SELECT * FROM " + parseName(tableName) + " WHERE `id`=:id FOR UPDATE", ImmutableMap.of("id", id));
    }

    /**
     * 查找第一条数据
     *
     * @return 第一条被添加的数据
     */
    @Override
    public T get() {
        return get("SELECT * FROM " + parseName(tableName) + " LIMIT 1", null);
    }

    @Override
    public T getForUpdate() {
        return get("SELECT * FROM " + parseName(tableName) + " LIMIT 1 FOR UPDATE", null);
    }

    @Override
    public T getForUpdate(String conditionSql, Map<String, Object> argMap) {
        if (argMap == null) {
            throw new RuntimeException("argMap不能为空");
        }
        argMap = changeEum(argMap);
        String sql = "SELECT * FROM " + parseName(tableName) + " WHERE " + conditionSql + " LIMIT 1 FOR UPDATE";
        return get(sql, argMap);
    }

    @Override
    public T get(String conditionSql, Map<String, Object> argMap) {
        List<T> list = this.list(conditionSql, argMap);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public <IdEntity extends IdEntityBasePure> void add(IdEntity t) {
        MySqlHelper.PrepareSqlParamResult ret = prepareSqlParamForAdd(t);
        String sql = "INSERT INTO " + parseName(tableName) + " " + ret.getSql();
        //获取插入数据后的id
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJt().update(sql, ret.getMap(), keyHolder);
        if (keyHolder.getKey() != null) {
            t.setId(keyHolder.getKey().intValue());
        }
    }

    @Override
    public <Entity> int addBatch(Collection<Entity> entities) {
        if (entities != null && !entities.isEmpty()) {
            PrepareSqlParamResult result = prepareSqlParamForBatchAdd(entities.iterator().next());
            String sql = "INSERT INTO " + parseName(tableName) + " " + result.getSql();
            //获取的是每个index下都对应的一个map。
            SqlParameterSource[] sources = prepareSqlParams(entities);
            int[] ints = getJt().batchUpdate(sql, sources);
            return ints.length;
        } else {
            return 0;
        }
    }

    @Override
    public List<T> list() {
        return this.list("SELECT * FROM " + parseName(tableName), null);
    }

    @Override
    public List<T> list(String sql, Map<String, Object> argMap) {
        argMap = changeEum(argMap);
        //使用beanpropertyrowmapper将数据库查询结果转换成java类对象，获取list结果列表，数据库表字段和实体类自动对应 persistentclass代表要操作的实体
        @SuppressWarnings({"unchecked", "rawtypes"}) List<T> list = getJt().query(sql, argMap, new BeanPropertyRowMapper(persistentClass));
        return list == null ? new ArrayList<T>() : list;
    }

    @Override
    public <Entity> void add(Entity t) {
        PrepareSqlParamResult result = prepareSqlParamForAdd(t);
        String sql = "INSERT INTO " + parseName(tableName) + " " + result.getSql();
        getJt().update(sql, result.getMap());
    }


    @Override
    public int getCount(String sql, Map<String, Object> argMap) {
        argMap = changeEum(argMap);
        StringBuilder sb = new StringBuilder("SELECT COUNT(0) `cnt` FROM (");
        sb.append(sql);
        sb.append(") AS `_tn`");
        return (int) (long) getSimple(sb.toString(), argMap).get("cnt");
    }

    @Override
    public PageBean<T> list(PageBean<T> pageBean, String sql, Map<String, Object> argMap) {
        argMap = changeEum(argMap);
        int startRow = pageBean.getStartRow();
        //一页的记录数
        @Max(100) int pageSize = pageBean.getPageSize();
        StringBuilder sb = new StringBuilder(sql);
        int count = this.getCount(sb.toString(), argMap);
        //总记录数
        pageBean.setRowCount(count);
        if (StringUtils.isNotBlank(pageBean.getOrderField()) && propList.contains(pageBean.getOrderField().toLowerCase())) {
            sb.append(" ORDER BY ");
            sb.append(parseName(pageBean.getOrderField()));
            if (StringUtils.isNotBlank(pageBean.getOrderDirection())) {
                sb.append(" ");
                sb.append(pageBean.getOrderDirection());
            }
            if (propList.contains("id") && !"id".equalsIgnoreCase(pageBean.getOrderField())) {
                sb.append(",`id` ASC");
            }
        }
        sb.append(" LIMIT ");
        sb.append(startRow);
        sb.append(",");
        sb.append(pageSize);

        @SuppressWarnings({"rawtypes", "unchecked"}) List<T> data = getJt().query(sb.toString(), argMap, new BeanPropertyRowMapper(persistentClass));
        pageBean.setData(data);
        return pageBean;
    }

    @Override
    public PageBean<T> list(PageBean<T> pageBean, String sql) {
        return list(pageBean, sql, null);
    }

    @Override
    public List<T> simpleList(String sql, Map<String, Object> argMap) {
        return getJt().queryForList(sql, argMap, persistentClass);
    }
}
