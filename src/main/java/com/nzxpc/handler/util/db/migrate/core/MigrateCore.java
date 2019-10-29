package com.nzxpc.handler.util.db.migrate.core;

import com.nzxpc.handler.util.LogUtil;
import com.nzxpc.handler.util.encrypt.Md5Util;
import com.nzxpc.handler.util.db.migrate.function.ColumnAnnotationHandler;
import com.nzxpc.handler.util.db.migrate.function.TableAnnotationHandler;
import com.nzxpc.handler.util.db.migrate.handler.DefaultHandler;
import com.nzxpc.handler.util.db.migrate.model.ColumnModel;
import com.nzxpc.handler.util.db.migrate.model.KeyModel;
import com.nzxpc.handler.util.db.migrate.model.MigrateLog;
import com.nzxpc.handler.util.db.migrate.model.TableModel;
import com.nzxpc.handler.util.validate.Display;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
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

    private void saveMigrateSql() throws SQLException {
        this.jdbc.executeBatch("INSERT INTO `MigrateLog` (`sql`, `success`, `createAt`) VALUES (?, ?, ?)", migrateLogList, (ps, item) -> {
            ps.setString(1, item.getSql());
            ps.setBoolean(2, item.isSuccess());
            ps.setString(3, item.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        });
    }

    public void start() {
        boolean hasError = false;

        try {
            this.loadMetadata();
            this.beginMigrate();
        } catch (Exception e) {
            LogUtil.err(MigrateCore.class, e);
            hasError = true;
        } finally {
            try {
                this.saveMigrateSql();
            } catch (SQLException e) {
                LogUtil.err(MigrateCore.class, e);
                hasError = true;
            } finally {
                this.jdbc.close();
            }
        }
        if (hasError) {
            System.exit(1);
        }
        LogUtil.info(MigrateCore.class, String.format("Database [ %s ] initialization complete.", dbName));
    }

    private void beginMigrate() throws SQLException {
        //表重命名
        for (Map.Entry<String, TableModel> entry : oldEntities.entrySet()) {
            TableModel table = entry.getValue();
            String oldKey = table.OLD_TABLE_NAME.toLowerCase();
            String newKey = table.TABLE_NAME.toLowerCase();
            if (tables.containsKey(oldKey) && tables.containsKey(newKey)) {
                throw new SQLException(String.format("重命名表失败，新表%s和旧表%s同时存在", table.TABLE_NAME, table.OLD_TABLE_NAME));
            } else if (tables.containsKey(oldKey)) {
                execSql("RENAME TABLE `%s` TO `%s`", table.OLD_TABLE_NAME, table.TABLE_NAME);
                tables.put(newKey, tables.remove(oldKey));
            }

        }
        //修改
    }

    private void migrateTable(TableModel table, TableModel dbTable) throws SQLException {
        table.sortColumn();
        if (dbTable != null) {
            LinkedHashMap<String, ColumnModel> dbColumns = dbTable.columns;
            LinkedHashMap<String, ColumnModel> columns = table.columns;
            List<String> sqls = new ArrayList<>();
            List<String> keysql = new ArrayList<>();
            List<String> modify = new ArrayList<>();
            //字段重命名
            for (Map.Entry<String, ColumnModel> entry : table.oldColumns.entrySet()) {
                String oldColumnName = entry.getKey();
                ColumnModel column = entry.getValue();
                if (dbColumns.containsKey(oldColumnName) && dbColumns.containsKey(column.COLUMN_NAME)) {
                    throw new SQLException(String.format("重命名字段失败: 旧字段[ %s ] 与新字段[ %s ] 同时存在", oldColumnName, column.COLUMN_NAME));
                } else if (dbColumns.containsKey(oldColumnName)) {
                    execSql(String.format("ALTER TABLE `%s` CHANGE COLUMN `%s` %s", table.TABLE_NAME, oldColumnName, column.toString()));
                    dbColumns.remove(oldColumnName);
                    dbColumns.put(column.COLUMN_NAME, column);
                }
            }
            //主键索引的不同
            if (dbTable.primaryKeys.size() == table.primaryKeys.size()) {
                Set<String> primaryKeys = new HashSet<>(dbTable.primaryKeys);
                primaryKeys.removeAll(table.primaryKeys);
                if (!primaryKeys.isEmpty()) {
                    sqls.add("DROP PRIMARY KEY");
                    if (!table.primaryKeys.isEmpty()) {
                        keysql.add(String.format("ADD PRIMARY KEY (%s)", table.joinPrimaryKeys()));
                    }
                }
            } else {
                if (!dbTable.primaryKeys.isEmpty()) {
                    sqls.add("DROP PRIMARY KEY");
                }
                if (!table.primaryKeys.isEmpty()) {
                    keysql.add(String.format("ADD PRIMARY KEY (%s)", table.joinPrimaryKeys()));
                }
            }
            //唯一约束索引的不同
            LinkedHashMap<String, String> dbUks = dbTable.uniqueKeys;
            LinkedHashMap<String, String> uks = table.uniqueKeys;
            dbUks.forEach((uk, name) -> {
                if (!uks.containsKey(uk)) {
                    sqls.add(String.format("DROP INDEX `%s`", name));
                }
            });
            uks.forEach((uk, name) -> {
                if (!dbUks.containsKey(uk)) {
                    sqls.add(String.format("ADD UNIQUE INDEX `uk_%s` (%s) ", Md5Util.md5Hex(String.format("%s_%s_%s", dbName, table.TABLE_NAME,
                            uk.replace("`", "").replace(",", "_"))), uk));
                }
            });
            // 其他索引不同
            LinkedHashMap<String, String> dbKeys = dbTable.keys;
            LinkedHashMap<String, String> keys = table.keys;
            dbKeys.forEach((key, name) -> {
                if (!keys.containsKey(key)) {
                    sqls.add(String.format("DROP INDEX `%s`", name));
                }
            });
            keys.forEach((key, name) -> {
                if (!dbKeys.containsKey(key)) {
                    sqls.add(String.format("ADD INDEX `idx_%s` (%s) ", Md5Util.md5Hex(String.format("%s_%s_%s", dbName, table.TABLE_NAME,
                            key.replace("`", "").replace(",", "_"))), key));
                }
            });
            // 字段的不同
            dbColumns.forEach((name, column) -> {
                if (!columns.containsKey(name)) {
                    sqls.add("DROP COLUMN `" + name + "`");
                }
            });
            List<ColumnModel> addNotNullColumns = new ArrayList<>();
            // 字段修改
            for (String name : columns.keySet()) {
                ColumnModel column = columns.get(name);
                if (dbColumns.containsKey(name)) {
                    ColumnModel dbColumn = dbColumns.get(name);
                    if (!column.toTypeString().equalsIgnoreCase(dbColumn.toTypeString())) {
                        if (StringUtils.equals(dbColumn.IS_NULLABLE, "YES")
                                && StringUtils.equals(column.IS_NULLABLE, "NO")) {
                            // 更新NULL值为默认值
                            execSql(String.format("UPDATE `%s` SET `%s`=? WHERE `%s` IS NULL",
                                    table.TABLE_NAME, name, name), column.getDataType().defaultValue);
                        }
                        modify.add("MODIFY COLUMN " + column.toString());
                    }
                } else {
                    // 判断 如果是非空, 并且不能设置默认值，则先设置成可以为NULL
                    if ("NO".equals(column.IS_NULLABLE)
                            && !column.getDataType().canSetDefaultValue) {
                        addNotNullColumns.add(column);
                        column.IS_NULLABLE = "YES";
                    }
                    sqls.add("ADD COLUMN " + column.toString());
                }
            }
            if (modify.size() > 0) {
                execSql(String.format("ALTER TABLE `%s` %s", table.TABLE_NAME, StringUtils.join(modify.toArray(), ",")));
            }
            sqls.addAll(keysql);
            if (sqls.size() > 0) {
                execSql(String.format("ALTER TABLE `%s` %s", table.TABLE_NAME, StringUtils.join(sqls.toArray(), ",")));
            }
            sqls.clear();
            modify.clear();
            for (ColumnModel column : addNotNullColumns) {
                column.IS_NULLABLE = "NO";
                // 更新NULL值为默认值
                execSql(String.format("UPDATE `%s` SET `%s`=? WHERE `%s` IS NULL",
                        table.TABLE_NAME, column.COLUMN_NAME, column.COLUMN_NAME),
                        column.getDataType().defaultValue);
                modify.add("MODIFY COLUMN " + column.toString());
            }
            if (modify.size() > 0) {
                execSql(String.format("ALTER TABLE `%s` %s", table.TABLE_NAME, StringUtils.join(modify.toArray(), ",")));
            }
        } else {
            // 如果不存在则创建表
            StringBuilder ct = new StringBuilder("CREATE TABLE `");
            ct.append(table.TABLE_NAME).append("`");
            StringJoiner joiner = new StringJoiner(", ", " (", ") ");
            joiner.setEmptyValue("");
            table.columns.forEach((name, column) -> joiner.add(column.toString()));

            // 主键
            if (!table.primaryKeys.isEmpty()) {
                joiner.add(String.format("PRIMARY KEY (%s)", table.joinPrimaryKeys()));
            }
            // 唯一约束
            table.uniqueKeys.keySet().forEach(key -> joiner.add(String.format("UNIQUE KEY `uk_%s` (%s)", Md5Util.md5Hex(String.format("%s_%s_%s",
                    dbName, table.TABLE_NAME, key.replace("`", "").replace(",", "_"))), key)));
            // 索引
            table.keys.keySet().forEach(key -> joiner.add(String.format("KEY `idx_%s` (%s)", Md5Util.md5Hex(String.format("%s_%s_%s",
                    dbName, table.TABLE_NAME, key.replace("`", "").replace(",", "_"))), key)));
            ct.append(joiner.toString());
            execSql(ct.toString());
        }
    }

    private void execSql(String sql, Object... args) throws SQLException {
        LogUtil.info(sql);
        MigrateLog migrateLog = new MigrateLog();
        migrateLogList.add(migrateLog);
        migrateLog.setSql(sql);
        try {
            jdbc.execute(sql, args);
            migrateLog.setSuccess(true);
        } catch (Exception e) {
            LogUtil.err("执行失败", e);
            migrateLog.setSuccess(false);
            throw e;
        }
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

        columnModels.forEach(c -> tables.get(c.TABLE_NAME.toLowerCase()).columns.put(c.COLUMN_NAME, c));
        // 获取所有的实体类
        List<Class<?>> entityClasses = listEntityByPackage();
        if (entityClasses.size() == 1) {
            if (tables.size() > 1 || (tables.size() == 1 && !tables.containsKey("MigrateLog".toLowerCase()))) {
                throw new RuntimeException(String.format("数据库中已有数据表, 但包 [ %s ] 下没有找到实体类.", StringUtils.join(this.packageNames, ",")));
            }
        }
        entityClasses.forEach(this::reflectEntity);
    }


    /**
     * 反射获取对象所有属性
     */
    private void reflectEntity(final Class<?> clazz) {
        //一个类就是一个table
        TableModel tableModel = new TableModel();
        tableModel.TABLE_NAME = clazz.getSimpleName();
        TABLE_ANNOTATION_HANDLER_MAP.forEach((annotation, handler) -> {
            if (clazz.isAnnotationPresent(annotation)) {
                handler.accept(clazz.getAnnotation(annotation), tableModel);
            }
        });
        Class<?> cls = clazz;
        for (; cls != Object.class; cls = cls.getSuperclass()) {
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                int i = field.getModifiers();
                if (Modifier.isStatic(i) || Modifier.isFinal(i) || Modifier.isNative(i)) {
                    continue;
                }
                reflectField(field, tableModel);
            }
        }
        entities.put(tableModel.TABLE_NAME.toLowerCase(), tableModel);
        if (StringUtils.isNotBlank(tableModel.OLD_TABLE_NAME)) {
            oldEntities.put(tableModel.OLD_TABLE_NAME.toLowerCase(), tableModel);
        }
    }

    private void reflectField(Field field, TableModel table) {
        ColumnModel columnModel = new ColumnModel(table);
        columnModel.COLUMN_NAME = field.getName();
        final Class<?> fieldType = field.getType();
        COLUMN_ANNOTATION_HANDLER_MAP.forEach((annotation, handler) -> {
            if (field.isAnnotationPresent(annotation)) {
                handler.accept(field.getAnnotation(annotation), columnModel);
            }
        });
        if (fieldType.isEnum() || fieldType.isPrimitive()) {
            columnModel.IS_NULLABLE = "NO";
        }
        if (fieldType.isEnum()) {
            if (columnModel.COLUMN_COMMENT == null) {
                columnModel.COLUMN_COMMENT = "";
            } else {
                columnModel.COLUMN_COMMENT += ":";
            }
        }
        StringJoiner sj = new StringJoiner(",");
        sj.setEmptyValue("");
        Field[] fields = fieldType.getDeclaredFields();
        if (fields != null) {
            for (Field field1 : fields) {
                if (field1.isAnnotationPresent(Display.class)) {
                    Display display = field1.getAnnotation(Display.class);
                    sj.add(String.format("%s(%s)", field1.getName(), display.value()));
                } else {
                    sj.add(field1.getName());
                }
            }
        }
        columnModel.COLUMN_COMMENT += sj.toString();
        if (StringUtils.isBlank(columnModel.COLUMN_COMMENT)) {
            DataType type = DATA_TYPE_MAP.get(fieldType.isEnum() ? byte.class : fieldType);
            if (type == null) {
                LogUtil.err(String.format("不支持的类型，name=%s,value=%s", field.getName(), field.getType()));
            } else {
                columnModel.COLUMN_TYPE = type.toString(columnModel);
            }
        }
        table.columns.put(columnModel.COLUMN_NAME, columnModel);
        if (StringUtils.isNotBlank(columnModel.OLD_COLUMN_NAME)) {
            table.oldColumns.put(columnModel.OLD_COLUMN_NAME, columnModel);
        }

    }


    /**
     * 获取所有的实体类, 支持多个包
     */
    private List<Class<?>> listEntityByPackage() {
        List<Class<?>> ret = new ArrayList<>();
        ret.add(MigrateLog.class); // 添加迁移记录实体
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
            for (String packageName : packageNames) {
                for (BeanDefinition beanDefinition : scanner.findCandidateComponents(packageName)) {
                    Class<?> entity = Class.forName(beanDefinition.getBeanClassName());
                    ret.add(entity);
                }
            }
        } catch (Exception e) {
            LogUtil.err("", e);
        }
        return ret;
    }
}
