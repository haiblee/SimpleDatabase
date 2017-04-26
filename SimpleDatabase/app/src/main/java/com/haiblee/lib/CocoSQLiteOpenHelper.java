package com.haiblee.lib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.SparseArray;

/**
 * Created by Haibiao.Li on 2017/4/21 0021 10:12.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

final class CocoSQLiteOpenHelper extends SQLiteOpenHelper{
    private static final String TAG = "CocoSQLiteOpenHelper";
    private final AbstractDatabase mDatabase;

    CocoSQLiteOpenHelper(Context context, AbstractDatabase database) {
        super(context, database.databaseName(), null, database.databaseVersion());
        mDatabase = database;
    }

    private static boolean isAPI(int api){
        return Build.VERSION.SDK_INT >= api;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = super.getReadableDatabase();
        if(isAPI(16)){
            if(!db.isWriteAheadLoggingEnabled()){
                db.enableWriteAheadLogging();
            }
        }else{
            db.enableWriteAheadLogging();
        }
        return db;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db =  super.getWritableDatabase();
        if(isAPI(16)){
            if(!db.isWriteAheadLoggingEnabled()){
                db.enableWriteAheadLogging();
            }
        }else{
            db.enableWriteAheadLogging();
        }
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        SparseArray<ITable[]> arrays = mDatabase.staticTables();
        if(arrays != null){
            for(int i = 0 ; i < arrays.size() ; i++){
                ITable[] tables = arrays.valueAt(i);
                for(ITable table : tables){
                    String[] sqlArr = table.createTableSQL();
                    for(String sql : sqlArr){
                        db.execSQL(sql);
                    }
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SparseArray<ITable[]> staticArrays = mDatabase.staticTables();
        if(staticArrays != null){
            for(int i = 0 ; i < staticArrays.size() ; i++){
                //这张表创建时，数据库的版本
                final int dbVersionWhenTableCreate = staticArrays.keyAt(i);
                //执行创建表
                if(dbVersionWhenTableCreate > oldVersion && dbVersionWhenTableCreate <= newVersion){
                    ITable[] addedTables = staticArrays.valueAt(i);
                    for(ITable table : addedTables){
                        String[] sqlArr = table.createTableSQL();
                        for(String sql : sqlArr){
                            db.execSQL(sql);
                        }
                    }
                }
                //必须在表创建之后才能修改
                if(newVersion > dbVersionWhenTableCreate){
                    ITable[] tables = staticArrays.valueAt(i);
                    for(ITable table : tables){
                        //版本依次递增，执行修改表语句
                        for(int version = Math.max(dbVersionWhenTableCreate,oldVersion) + 1 ; version <= newVersion ; version++){
                            String[] sqlArray = table.alterTableSQL(version);
                            if(sqlArray != null){
                                for(String sql : sqlArray){
                                    db.execSQL(sql);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
