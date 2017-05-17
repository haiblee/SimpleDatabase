package com.haiblee.lib;

/**
 * Created by Haibiao.Li on 2017/3/15 0015 12:12.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class TableField {
    public static final int FLAG_NOT_NULL               = 1 << 0;
    public static final int FLAG_PRIMARY_KEY            = 1 << 1;
    public static final int FLAG_AUTO_INCREMENT         = 1 << 2;
    public static final int FLAG_INDEX_KEY              = 1 << 3;
    public static final int FLAG_UNIQUE                 = 1 << 4;

    public enum Typed {
        NULL, INTEGER, REAL, TEXT, BLOB
    }

    private final String name;
    private Typed typed;
    private Object defValue;
    private int attrFlag;

    public TableField(String name) {
        this(name, Typed.TEXT);
    }

    public TableField(String name, Typed typed) {
        this(name,typed,0);
    }

    public TableField(String name, int attrFlag) {
        this(name, Typed.TEXT,attrFlag);
    }

    public TableField(String name, Typed typed, int flag){
        this.name = name;
        this.typed = typed;
        this.attrFlag = flag;
    }

    public String getName() {
        return name;
    }

    public Typed getTyped() {
        return typed;
    }

    public Object getDefValue() {
        return defValue;
    }

    public TableField setDefValue(Object defValue) {
        this.defValue = defValue;
        return this;
    }

    public void setAttrFlag(int flag){
        this.attrFlag = flag;
    }

    public boolean hasAttr(int flag){
        return (attrFlag & flag) == flag;
    }

    @Override
    public String toString() {
        return "TableField{" +
                "name='" + name + '\'' +
                ", typed=" + typed +
                ", defValue=" + defValue +
                ", attrFlag=" + Integer.toHexString(attrFlag) +
                '}';
    }
}
