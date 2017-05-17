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

final class CocoSQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "CocoSQLiteOpenHelper";
    private final AbstractDatabase mDatabase;

    CocoSQLiteOpenHelper(Context context, AbstractDatabase database) {
        super(context, database.databaseName(), null, database.databaseVersion());
        mDatabase = database;
        if(isAPI(16) && database.isNeedEnableWAL()){
            this.setWriteAheadLoggingEnabled(true);
        }
    }

    private static boolean isAPI(int api){
        return Build.VERSION.SDK_INT >= api;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        SLog.d(TAG,"onOpen：db hashCode = %s,dbPath = %s",db.hashCode(),db.getPath());
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        SLog.d(TAG,"onConfigure：db hashCode = %s,dbPath = %s",db.hashCode(),db.getPath());
        if(isAPI(11) && mDatabase.isNeedEnableWAL()){
            db.enableWriteAheadLogging();
        }
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
        synchronized (mDatabase){
            if(mDatabase.getFlagValue(AbstractDatabase.FLAG_CREATED)){
                SLog.w(TAG, String.format("onCreate：the database(%s,%s) FLAG_CREATED value is true,create completed,don't need to repeat",mDatabase.hashCode(),mDatabase.databaseName()));
                return;
            }
            SparseArray<ITable[]> arrays = mDatabase.staticTables();
            SLog.d(TAG, String.format("onCreate: mDatabase(%s,%s),table arrays size = %s",mDatabase.hashCode(),mDatabase.databaseName(),(arrays == null ? "null" : arrays.size())));
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
            mDatabase.setFlagValue(AbstractDatabase.FLAG_CREATED,true);
        }
    }

    @Override
    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        synchronized (mDatabase){
            if(mDatabase.getFlagValue(AbstractDatabase.FLAG_UPGRADED)){
                SLog.w(TAG, String.format("onUpgrade：the database(%s,%s) FLAG_UPGRADED value is true,upgrade completed,don't need to repeat",mDatabase.hashCode(),mDatabase.databaseName()));
                return;
            }
            SparseArray<ITable[]> staticArrays = mDatabase.staticTables();
            SLog.d(TAG, String.format("onUpgrade: mDatabase(%s,%s),table staticArrays size = %s",mDatabase.hashCode(),mDatabase.databaseName(),(staticArrays == null ? "null" : staticArrays.size())));
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
                            for(int version = Math.max(dbVersionWhenTableCreate,oldVersion) + 1; version <= newVersion ; version++){
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
            mDatabase.setFlagValue(AbstractDatabase.FLAG_UPGRADED,true);
        }
    }
}
