package com.nzxpc.handler.util.db.migrate.model;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class TableModel {

    public String TABLE_NAME;
    public String OLD_TABLE_NAME;

    public LinkedHashMap<String, ColumnModel> columns = new LinkedHashMap<>();
    public LinkedHashMap<String, ColumnModel> oldColumns = new LinkedHashMap<>();
    public LinkedHashSet<String> primaryKeys = new LinkedHashSet<>();
    public LinkedHashMap<String, String> uniqueKeys = new LinkedHashMap<>();
    public LinkedHashMap<String, String> keys = new LinkedHashMap<>();

    // 是联合索引时查询判断用, 只在本类中使用
    private HashMap<String, String> keyMap = new HashMap<>();

    public void addKey(String keyName, boolean unique, String... columnNames) {
        boolean union = StringUtils.isNotBlank(keyName);
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = String.format("`%s`", columnNames[i]);
        }

        List<String> cols = new ArrayList<>(Arrays.asList(columnNames));
        if (union && keyMap.containsKey(keyName)) {
            String s = keyMap.get(keyName);
            cols.addAll(Arrays.asList(s.split(",")));
            if (unique) {
                uniqueKeys.remove(s);
            } else {
                keys.remove(s);
            }
        }
        cols.sort(String::compareToIgnoreCase);
        String join = StringUtils.join(cols.toArray(), ",");
        if (unique) {
            uniqueKeys.put(join, keyName);
        } else {
            keys.put(join, keyName);
        }
        if (union) {
            keyMap.put(keyName, join);
        }

    }

    public void addPrimaryKey(String... columnNames) {
        primaryKeys.addAll(Arrays.asList(columnNames));
    }

    public String joinPrimaryKeys() {
        StringJoiner sj = new StringJoiner(",");
        sj.setEmptyValue("");
        primaryKeys.forEach(item -> sj.add(String.format("`%s`", item)));
        return sj.toString();
    }

    public void sortColumn() {
        LinkedHashMap<String, ColumnModel> sorted = new LinkedHashMap<>();
        primaryKeys.forEach(item -> sorted.put(item, columns.get(item)));
        columns.forEach((a, b) -> {
            if (!primaryKeys.contains(a)) {
                sorted.put(a, b);
            }
        });
        columns = sorted;
    }
}
