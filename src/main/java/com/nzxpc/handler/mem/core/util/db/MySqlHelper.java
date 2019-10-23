package com.nzxpc.handler.mem.core.util.db;

import com.nzxpc.handler.mem.core.entity.Getter;
import com.nzxpc.handler.mem.core.entity.IdEntityBasePure;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MySqlHelper<T> extends SqlHelper<T> {

    protected MySqlHelper() {
    }

    public MySqlHelper(Class<T> persistentClass, JdbcTemplate jt) {
        tableName = persistentClass.getSimpleName();
        this.persistentClass = persistentClass;

        setJdbcTemplate(jt);
    }

    /**
     * 获取属性名,实体（Class）中所有的字段名,不包括class
     *
     * @return 属性名列表
     */
    protected List<String> getPropList(Class<?> clazz) {
        List<String> propList = new ArrayList<>();

        return propList;
    }


    @Override
    protected PrepareSqlParamResult prepareSqlParamForUpdate(Object bean, List<String> updateColumns) {
        return null;
    }

    @Override
    protected PrepareSqlParamResult prepareSqlParamForAdd(Object bean) {
        return null;
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
