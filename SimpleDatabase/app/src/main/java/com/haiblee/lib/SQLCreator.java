package com.haiblee.lib;


/**
 * Created by Haibiao.Li on 2017/3/15 0015 12:21.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class SQLCreator {
    private static final String TAG = "SQLCreator";
    public static boolean DEBUG = true;
    private static final String BLANK = " ";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    private static final String CREATE_INDEX = "CREATE INDEX";
    private static final String CREATE_UNIQUE_INDEX = "CREATE UNIQUE INDEX";
    private static final String ON = "ON";
    private static final String DEFAULT = "DEFAULT";
    private static final String UNIQUE = "UNIQUE";
    private static final String INDEX = "INDEX";
    private static final String PRIMARY_KEY = "PRIMARY KEY";
    private static final String AUTOINCREMENT = "AUTOINCREMENT";
    private static final String NOT_NULL = "NOT NULL";
    private static final String BRACKET_LEFT = "(";
    private static final String BRACKET_RIGHT = ")";
    private static final String FORMAT_SINGLE_QT = "'%s'";
    private static final String SEMICOLON = ";";
    private static final String COMMA = ",";
    private static final String ALTER = "ALTER";
    private static final String TABLE = "TABLE";
    private static final String ADD = "ADD";
    private static final String DROP = "DROP";
    private static final String DOT = ".";

    public static String createTableSQL(String tableName, TableField[] fields){
        StringBuilder b = new StringBuilder();
        b.append(CREATE_TABLE).append(BLANK);
        b.append(String.format(FORMAT_SINGLE_QT,tableName)).append(BLANK);
        b.append(BRACKET_LEFT);
        for(int i = 0 ; i < fields.length ; i++){
            if(fields[i] == null){
                continue;
            }
            b.append(createFieldRowSQL(fields[i]));
            if(i < fields.length - 1){
                b.append(COMMA);
            }
        }
        b.append(BRACKET_RIGHT);
        if(DEBUG) SLog.d(TAG, String.format("createTableSQL：[%s]",b.toString()));
        return b.toString();
    }

    private static String createFieldRowSQL(TableField field){
        if(field == null){
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append(field.getName());
        b.append(BLANK);
        b.append(field.getTyped());
        b.append(BLANK);
        if(field.hasAttr(TableField.FLAG_PRIMARY_KEY)){
            b.append(PRIMARY_KEY);
            b.append(BLANK);
        }
        if(field.hasAttr(TableField.FLAG_AUTO_INCREMENT)){
            b.append(AUTOINCREMENT);
            b.append(BLANK);
        }
        if(field.getDefValue() != null){
            b.append(DEFAULT).append(BLANK);
            b.append(field.getDefValue().toString());
            b.append(BLANK);
        }else{
            if(field.hasAttr(TableField.FLAG_NOT_NULL)){
                b.append(NOT_NULL);
                b.append(BLANK);
            }
        }
        if(DEBUG) SLog.d(TAG, String.format("createFieldRowSQL：[%s]，TableField = [%s]",b.toString(),field.toString()));
        return b.toString();
    }

    public static String createIndex(String tableName, TableField field){
        StringBuilder b = null;
        if(field.hasAttr(TableField.FLAG_INDEX_KEY)){
            b = new StringBuilder();
            if(field.hasAttr(TableField.FLAG_UNIQUE)){
                b.append(CREATE_UNIQUE_INDEX);
            }else{
                b.append(CREATE_INDEX);
            }
            b.append(BLANK);
            b.append(tableName);
            b.append("_");
            b.append(field.getName()).append(BLANK);
            b.append(ON).append(BLANK);
            b.append(tableName).append(BLANK);
            b.append(BRACKET_LEFT).append(field.getName()).append(BRACKET_RIGHT);
            b.append(SEMICOLON);
        }
        String result = b == null ? null : b.toString();
        if(DEBUG) SLog.d(TAG, String.format("createIndex：[%s]，TableField = [%s]",result,field.toString()));
        return result;
    }

    public static String addTableField(String tableName, TableField field){
        return ALTER + BLANK + TABLE + BLANK +
                tableName + BLANK + ADD + BLANK +
                field.getName() + BLANK + field.getTyped().toString();
    }

    public static String dropTable(String dbName, String tableName){
        return DROP + BLANK + TABLE + BLANK + dbName + DOT + tableName;
    }

    public static void main(String[] args){
        SQLCreator.DEBUG = false;
        TableField[] fields = new TableField[10];
        for(int i = 0 ; i < fields.length ; i++){
            if(i == 2){
                fields[i] = new TableField("Name"+ i, TableField.Typed.INTEGER,TableField.FLAG_PRIMARY_KEY | TableField.FLAG_AUTO_INCREMENT);
            }else{
                fields[i] = new TableField("Name"+ i);
                if(i == 3){
                    fields[i].setAttrFlag(TableField.FLAG_NOT_NULL);
                    fields[i].setDefValue("哈哈欧");
                }
                if(i == 4){
                    fields[i].setAttrFlag(TableField.FLAG_INDEX_KEY | TableField.FLAG_NOT_NULL | TableField.FLAG_UNIQUE);
                }
            }
        }
        System.out.println("CreateTable: " + createTableSQL("Goods",fields));
        System.out.println("CreateIndex: "+ createIndex("Goods",fields[4]));

        System.out.println("二进制值为："+ Integer.toBinaryString(10));
    }

}
