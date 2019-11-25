package com.nzxpc.handler.util.db.migrate.core;

import com.nzxpc.handler.util.db.migrate.model.ColumnModel;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public enum DataType {
    Bit("bit", 0, true),
    Tinyint("tinyint", 0, true),
    Smallint("smallint", 0, true),
    Mediumint("mediumint", 0, true),
    Int("int", 0, true),
    Bigint("bigint", 0, true),
    Float("float", 0, true),
    Double("double", 0, true),
    Numeric("numeric", 0, true),
    Decimal("decimal", 0, true),
    Char("char", "", true),
    Varchar("varchar", "", true),
    Datetime("datetime", LocalDateTime.parse("1970-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), true),
    Timestamp("timestamp", 0, true),
    Date("date", LocalDate.parse("1970-01-01"), true),
    Time("time", LocalTime.parse("00:00:00"), true),

    // 下面这些都是为了更新默认值
    Year("year", java.time.Year.parse("1970"), true),
    Tinytext("tinytext", "", false),
    Text("tinytext", "", false),
    Mediumtext("tinytext", "", false),
    Longtext("tinytext", "", false),
    TinyBlob("tinytext", "", false),
    Blob("blob", "", false),
    Mediumblob("tinytext", "", false),
    Longblob("tinytext", "", false),
    // enum
    // set
    ;
    public String typeName;
    public Object defaultValue;
    public boolean canSetDefaultValue;

    DataType(String typeName, Object defaultValue, boolean canSetDefaultValue) {
        this.typeName = typeName;
        this.defaultValue = defaultValue;
        this.canSetDefaultValue = canSetDefaultValue;
    }

    public static DataType valueOfDefinition(String columnDefinition) {
        String s = StringUtils.trim(columnDefinition.split("\\(")[0]).toLowerCase();
        return DataType.valueOf(StringUtils.capitalize(s));
    }


    public String toString(ColumnModel model) {
        switch (this) {
            case Bit:
                return String.format("%s(%d)", this.typeName, 1);
            case Int:
                return String.format("%s(%d)", this.typeName, model.NUMERIC_PRECISION == 0 ? 11 : model.NUMERIC_PRECISION);
            case Bigint:
                return String.format("%s(%d)", this.typeName, model.NUMERIC_PRECISION == 0 ? 20 : model.NUMERIC_PRECISION);
            case Tinyint:
                return String.format("%s(%d)", this.typeName, model.NUMERIC_PRECISION == 0 ? 4 : model.NUMERIC_PRECISION);
            case Smallint:
                return String.format("%s(%d)", this.typeName, model.NUMERIC_PRECISION == 0 ? 6 : model.NUMERIC_PRECISION);
            case Char:
                return String.format("%s(%d)", this.typeName, model.CHARACTER_MAXIMUM_LENGTH == 0 ? 255 : model.CHARACTER_MAXIMUM_LENGTH);
            case Varchar:
                return String.format("%s(%d)", this.typeName, model.CHARACTER_MAXIMUM_LENGTH == 0 ? 255 : model.CHARACTER_MAXIMUM_LENGTH);
            case Decimal:
                return String.format("%s(%d,%d)", this.typeName, model.NUMERIC_PRECISION == 0 ? 19 : model.NUMERIC_PRECISION, model.NUMERIC_SCALE == 0 ? 2 : model.NUMERIC_SCALE);
            case Datetime:
                return model.DATETIME_PRECISION == 0 ? this.typeName : String.format("%s(%d)", this.typeName, model.DATETIME_PRECISION);
            case Double:
            case Float:
            case Date:
            case Time:
            case Year:
            case Blob:
            case Timestamp:
                return this.typeName;
            default:
        }
        return this.typeName;
    }
}
