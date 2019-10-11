package com.nzxpc.handler.mem.core.util.db.migrate.core;

import com.nzxpc.handler.mem.core.util.db.migrate.function.ColumnAnnotationHandler;
import com.nzxpc.handler.mem.core.util.db.migrate.function.TableAnnotationHandler;
import com.nzxpc.handler.mem.core.util.db.migrate.handler.DefaultHandler;
import com.nzxpc.handler.mem.core.util.db.migrate.model.ColumnModel;
import com.nzxpc.handler.mem.core.util.db.migrate.model.KeyModel;
import com.nzxpc.handler.mem.core.util.db.migrate.model.MigrateLog;
import com.nzxpc.handler.mem.core.util.db.migrate.model.TableModel;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 迁移主类
 */
public class MigrateCore {
    private final static LinkedHashMap<Class<? extends Annotation>, ColumnAnnotationHandler> COLUMN_ANNOTATION_HANDLER_MAP = new LinkedHashMap<>();
    private final static LinkedHashMap<Class<? extends Annotation>, TableAnnotationHandler> TABLE_ANNOTATION_HANDLER_MAP = new LinkedHashMap<>();
    private final static Map<Class, DataType> DATA_TYPE_MAP = new HashMap<>();

    public static <T extends Annotation> void addColumnAnnotationHandler(Class<T> clazz, ColumnAnnotationHandler handler) {
        COLUMN_ANNOTATION_HANDLER_MAP.put(clazz, handler);
    }

    public static <T extends Annotation> void addTableAnnotationHandler(Class<T> clazz, TableAnnotationHandler handler) {
        TABLE_ANNOTATION_HANDLER_MAP.put(clazz, handler);
    }

    public static void addDataType(Class<?> clazz, DataType dataType) {
        DATA_TYPE_MAP.put(clazz, dataType);
    }

    static {
        DefaultHandler.register();
        DefaultHandler.defaultDataTypeMap();
    }

    private MysqlUtil jdbc;
    private String dbName;
    private Set<String> packageNames;
    private LinkedHashMap<String, TableModel> entities = new LinkedHashMap<>();
    private LinkedHashMap<String, TableModel> oldEntities = new LinkedHashMap<>();
    private LinkedHashMap<String, TableModel> tables = new LinkedHashMap<>();
    private List<MigrateLog> migrateLogList = new ArrayList<>();

    public MigrateCore(String url, String userName, String password, Set<String> packageNames) {
        this.packageNames = packageNames;
        this.jdbc = MysqlUtil.createUtil(url, userName, password, true);
    }

    private void loadMetadata() throws SQLException {

        this.dbName = this.jdbc.queryForObject("SELECT DATABASE()", rs -> rs.getString(1));
        final List<TableModel> tableName = this.jdbc.queryForList("SELECT TABLE_NAME FROM information_schema.`TABLES` WHERE TABLE_SCHEMA = ?", rs -> {
            TableModel tableModel = new TableModel();
            tableModel.TABLE_NAME = rs.getString("TABLE_NAME");
            return tableModel;
        }, dbName);

        tableName.forEach(t -> tables.put(t.TABLE_NAME.toLowerCase(), t));
        final List<ColumnModel> columnModels = this.jdbc.queryForList("SELECT * FROM information_schema.`COLUMNS` WHERE TABLE_SCHEMA = ?", rs -> {
            String table_name = rs.getString("TABLE_NAME");
            ColumnModel columnModel = new ColumnModel(tables.get(table_name.toLowerCase()));
            columnModel.TABLE_NAME = table_name;
            columnModel.COLUMN_NAME = rs.getString("COLUMN_NAME");
            columnModel.COLUMN_DEFAULT = rs.getString("COLUMN_DEFAULT");
            columnModel.IS_NULLABLE = rs.getString("IS_NULLABLE");
            columnModel.CHARACTER_MAXIMUM_LENGTH = rs.getLong("CHARACTER_MAXIMUM_LENGTH");
            columnModel.NUMERIC_PRECISION = rs.getInt("NUMERIC_PRECISION");
            columnModel.NUMERIC_SCALE = rs.getInt("NUMERIC_SCALE");
            columnModel.DATETIME_PRECISION = rs.getInt("DATETIME_PRECISION");
            columnModel.COLUMN_TYPE = rs.getString("COLUMN_TYPE");
            columnModel.EXTRA = rs.getString("EXTRA");
            columnModel.COLUMN_COMMENT = rs.getString("COLUMN_COMMENT");
            return columnModel;
        }, dbName);

        for (TableModel tableModel : tableName) {

            ArrayList<KeyModel> keyList = this.jdbc.queryForList(String.format("SHOW INDEX FROM %s.`%s`", dbName, tableModel.TABLE_NAME), rt -> {
                KeyModel model = new KeyModel();
                model.Table = rt.getString("Table");
                model.Non_unique = rt.getBoolean("Non_unique");
                model.Key_name = rt.getString("Key_name");
                model.Column_name = rt.getString("Column_name");
                model.Seq_in_index = rt.getInt("Seq_in_index");
                return model;
            });
            Map<String, List<KeyModel>> map = keyList.stream().collect(Collectors.groupingBy(item -> item.Key_name));
            map.forEach((keyName, keyLists) -> {
                if ("PRIMARY".equalsIgnoreCase(keyName)) {
                    keyLists.forEach(key -> tableModel.addPrimaryKey(key.Column_name));
                } else {
                    String[] columnNames = new String[keyLists.size()];
                    keyLists.sort(Comparator.comparingInt(i -> i.Seq_in_index));
                    for (int i = 0; i < keyLists.size(); i++) {
                        columnNames[i] = keyLists.get(i).Column_name;
                    }
                    tableModel.addKey(keyName, !keyLists.get(0).Non_unique, columnNames);
                }

            });
        }
    }
}
