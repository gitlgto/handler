package com.nzxpc.handler.mem.core.util.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nzxpc.handler.mem.core.util.LogUtil;
import com.nzxpc.handler.mem.core.util.ReflectUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageBean<T> implements Serializable {
    private static final long serialVersionUID = 6295579614989910451L;

    /**
     * 第几页，默认值1
     */
    @Min(1)
    private int pageNo;

    /**
     * 总记录数
     */
    private int rowCount;
    /**
     * 一页的记录数，默认20，最大100
     */
    @Max(100)
    private int pageSize;
    /**
     * 排序字段
     */
    @Size(max = 100)
    private String orderField;

    /**
     * 排序方向
     */
    @Size(max = 4)
    private String orderDirection = "desc";

    /**
     * 数据
     */
    private List<T> data = new ArrayList<>();

    /**
     * 汇总信息
     */
    private Map<String, Object> sumMap = new HashMap<>();

    /**
     * 设置按id降序排序
     */
    @Deprecated
    public void setOrderByIdDesc() {
        setOrderDirection("desc");
        setOrderField("createAt");
    }

    public PageBean(PageBean<?> pageBean) {
        this.pageNo = pageBean.pageNo;
        this.orderDirection = pageBean.orderDirection;
        this.orderField = pageBean.orderField;
        this.pageSize = pageBean.pageSize;
        this.rowCount = pageBean.rowCount;
        this.sumMap = pageBean.sumMap;
        setDefaultOrderField();
    }

    public PageBean(PageBean<?> pageBean, List<T> data) {
        this.pageNo = pageBean.pageNo;
        this.orderDirection = pageBean.orderDirection;
        this.orderField = pageBean.orderField;
        this.pageSize = pageBean.pageSize;
        this.rowCount = pageBean.rowCount;
        this.sumMap = pageBean.sumMap;
        this.data = data;
        setDefaultOrderField();
    }

    public PageBean() {
        pageSize = 20;
        pageNo = 1;
        setDefaultOrderField();
    }

    int getStartRow() {
        int startRow = (pageNo - 1) * pageSize;
        if (startRow < 0) {
            startRow = 0;
        }
        return startRow;
    }

    public PageBean(int pageNo, int pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        setDefaultOrderField();
    }

    public PageBean<T> setPageNo(int pageNo) {
        pageNo = pageNo <= 0 ? 1 : pageNo;
        this.pageNo = pageNo;
        return this;
    }

    public PageBean<T> setPageSize(int pageSize) {
        if (pageSize > 100) {
            this.pageSize = 100;
        } else {
            this.pageSize = pageSize;
        }
        return this;
    }

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @JsonIgnore
    private T _item;

    @JsonIgnore
    public T get_item() {
        if (_item == null) {
            _item = getItemObject();
        }
        return _item;
    }

    @JsonIgnore
    public T getItemObject() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            Class<?> clazz = (Class) types[0];
            try {
                @SuppressWarnings("unchecked")
                T t = (T) clazz.getDeclaredConstructor().newInstance();
                return t;
            } catch (Exception e) {
                LogUtil.err("", e);
            }
        }
        return null;
    }

    private void setDefaultOrderField() {
        if (StringUtils.isBlank(orderField)) {
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                if (types[0] instanceof Class) {
                    Class<?> aClass = (Class) types[0];
                    if (ReflectUtil.getField(aClass, "createAt") != null) {
                        orderField = "createAt";
                    }
                }
            }
        }
    }

}
