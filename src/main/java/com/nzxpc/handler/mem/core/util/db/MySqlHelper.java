package com.nzxpc.handler.mem.core.util.db;

import com.nzxpc.handler.mem.core.entity.Getter;
import com.nzxpc.handler.mem.core.entity.IdEntityBasePure;
import com.nzxpc.handler.mem.core.util.ReflectUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

public class MySqlHelper<T> extends SqlHelper<T> {

    protected MySqlHelper() {
    }

    public MySqlHelper(Class<T> persistentClass, JdbcTemplate jt) {
        tableName = persistentClass.getSimpleName();
        this.persistentClass = persistentClass;
        propList.addAll(getPropList(persistentClass));
        setJdbcTemplate(jt);
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
        ret.sql = sjName.toString() + "VALUES" + sjVal.toString();
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
        ret.sql = sjName.toString() + "VALUES" + sjVal.toString();
        ret.map = new MapSqlParameterSource(map);
        return ret;
    }




    @Override
    public boolean exist(String conditionSql, Map<String, Object> argMap) {
        return false;
    }

    @Override
    public <E> PageBean<E> query(PageBean<E> pageBean, String sql, Map<String, Object> map, Class<E> clazz) {
        return null;
    }

    @Override
    public <Entity> int update(Entity data, String conditionSql, Map<String, Object> conditionMap, String... updateColumns) {
        return 0;
    }

    @Override
    public <IdEntity extends IdEntityBasePure> int updateById(IdEntity data, String updateColumns) {
        return 0;
    }

    @Override
    public <IdEntity extends IdEntityBasePure, V> int updateById(IdEntity data, Getter<IdEntity, V>... getters) {
        return 0;
    }

    @Override
    public int deleteById(int id) {
        return 0;
    }

    @Override
    public int delete(String conditionSql, Map<String, Object> conditionArgMap) {
        return 0;
    }

    @Override
    public T getById(int id) {
        return null;
    }

    @Override
    public T getByIdForUpdate(int id) {
        return null;
    }

    @Override
    public T get() {
        return null;
    }

    @Override
    public T getForUpdate() {
        return null;
    }

    @Override
    public T getForUpdate(String conditionSql, Map<String, Object> argMap) {
        return null;
    }

    @Override
    public T get(String conditionSql, Map<String, Object> argMap) {
        return null;
    }

    @Override
    public <IdEntity extends IdEntityBasePure> void add(IdEntity t) {

    }

    @Override
    public <Entity> int addBatch(Collection<Entity> entities) {
        return 0;
    }

    @Override
    public List<T> list() {
        return null;
    }

    @Override
    public List<T> list(String sql, Map<String, Object> argMap) {
        return null;
    }

    @Override
    public <Entity> void add(Entity t) {

    }

    @Override
    public int getCount(String sql, Map<String, Object> argMap) {
        return 0;
    }

    @Override
    public PageBean<T> list(PageBean<T> pageBean, String sql, Map<String, Object> argMap) {
        return null;
    }

    @Override
    public PageBean<T> list(PageBean<T> pageBean, String sql) {
        return null;
    }

    @Override
    public List<T> simpleList(String sql, Map<String, Object> argMap) {
        return null;
    }
}
