package com.nzxpc.handler.mem.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)

public class PageBean<T> implements Serializable {
    private static final long serialVersionUID = 6295579614989910451L;

    @Min(value = 1)
    private int pageNo;

    private int rowCount;

    private int pageSize;

    private String orderField;

    private String orderDirection="desc";

    private List<T>data=new ArrayList<>();

    private Map<String,Object>sumMap=new HashMap<>();

    public void setOrderByIdDesc(){
        setOrderDirection("desc");
        setOrderField("createAt");
    }

}
