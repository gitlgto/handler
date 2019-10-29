package com.nzxpc.handler.util.db.migrate.core;

import com.nzxpc.handler.util.db.migrate.model.ColumnModel;
import org.apache.commons.lang3.StringUtils;

public enum DataType {

    Decimal("decimal", 0, true);
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


    public String toString(ColumnModel columnModel) {
        switch (this){
            case Decimal:
                return String.format("%s(%d,%d)",this.typeName,columnModel.NUMERIC_PRECISION==0?19:columnModel.NUMERIC_PRECISION,columnModel.NUMERIC_SCALE==0?2:columnModel.NUMERIC_SCALE);
                default:
        }
        return this.typeName;
    }
}
