package org.anyline.data.entity;

public class DataType {
    private String name;
    private boolean ignorePrecision;
    private boolean ignoreScale;

    public DataType(String name){
        this.name = name;
    }
    public DataType(String name, boolean ignorePrecision, boolean ignoreScale){
        this.name = name;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    public static DataType INT = new DataType("int");
}
