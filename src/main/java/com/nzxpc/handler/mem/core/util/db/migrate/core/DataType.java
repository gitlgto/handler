package com.nzxpc.handler.mem.core.util.db.migrate.core;

public enum DataType {

    Decimal("decimal", 0, true)

    ;
    public String typeName;
    public Object defaultValue;
    public boolean canSetDefaultValue;

    DataType(String typeName, Object defaultValue, boolean canSetDefaultValue) {
        this.typeName = typeName;
        this.defaultValue = defaultValue;
        this.canSetDefaultValue = canSetDefaultValue;
    }
}
