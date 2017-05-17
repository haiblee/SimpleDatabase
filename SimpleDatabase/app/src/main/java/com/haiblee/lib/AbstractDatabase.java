package com.haiblee.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;
import android.util.SparseArray;

import java.util.Arrays;


/**
 * Created by Haibiao.Li on 2017/4/20 0020 15:10.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 * 抽象数据库
 */

public abstract class AbstractDatabase implements IDatabaseExecutor {
    private static final String TAG = "AbstractDatabase";

    public static final int FLAG_CREATED = 0;
    public static final int FLAG_UPGRADED = 1;
    private final Context mContext;
    private volatile int mFlags = 0;
    private final IDatabaseExecutor mExecutor;
    private final String mDatabaseName;
    private volatile boolean isOpen = false;

    public AbstractDatabase(Context context, String name) {
        this(context,name, null);
    }

    /**
     * 创建一个数据库
     * @param context  context
     * @param executor 数据库执行器
     */
    public AbstractDatabase(Context context, String name, IDatabaseExecutor executor) {
        mContext = context;
        mDatabaseName = name;
        if(executor == null){
            if(isWriteAheadLoggingEnabled()){
                executor = new WALDatabaseExecutor(context,this);
            }else{
                executor = new LockDatabaseExecutor(context,this);
            }
        }
        mExecutor = executor;
        isOpen = true;
    }

    protected boolean isNeedEnableWAL(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public final boolean isWriteAheadLoggingEnabled(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && isNeedEnableWAL();
    }

    @Override
    public synchronized void close() {
        mExecutor.close();
        isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    private void throwIfNotOpen(){
        if(!isOpen){
            throw new IllegalStateException(String.format("%s already closed !",TAG));
        }
    }

    public String storagePath() {
        return mContext.getDatabasePath(databaseName()).getPath();
    }

    public final String databaseName(){
        return mDatabaseName;
    }

    public abstract SparseArray<ITable[]> staticTables();

    public abstract int databaseVersion();

    public final void setFlagValue(int flag,boolean bool){
        if(bool){
            mFlags =  (mFlags | (1 << flag));
        }else{
            mFlags = (mFlags & (~(1 << flag)));
        }
    }

    public final boolean getFlagValue(int flag){
        return ((mFlags >>> flag) & 1) == 1;
    }

    @Override
    public final long insert(String table, String nullColumnHack, ContentValues values) {
        throwIfNotOpen();
        long t1 = System.currentTimeMillis();
        long result = -1;
        try {
            result = mExecutor.insert(table,nullColumnHack,values);
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage(table, "insert", e, String.valueOf(values));
        }
        if(SLog.isDebug()){
            logDurationMessage(table,"insert", System.currentTimeMillis() - t1);
        }
        return result;
    }

    @Override
    public final int delete(String table, String whereClause, String[] whereArgs) {
        throwIfNotOpen();
        long t1 = System.currentTimeMillis();
        int result = 0;
        try {
            result = mExecutor.delete(table,whereClause,whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage(table, "delete", e, String.format("whereClause = %s，whereArgs = %s", whereClause, Arrays.toString(whereArgs)));
        }
        if(SLog.isDebug()){
            logDurationMessage(table,"delete", System.currentTimeMillis() - t1);
        }
        return result;
    }

    @Override
    public final int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        throwIfNotOpen();
        long t1 = System.currentTimeMillis();
        int result = 0;
        try {
            result = mExecutor.update(table,values,whereClause,whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage(table, "update", e, String.format("ContentValues = %s，whereClause = %s，whereArgs = %s", String.valueOf(values), whereClause, Arrays.toString(whereArgs)));
        }
        if(SLog.isDebug()){
            logDurationMessage(table,"update", System.currentTimeMillis() - t1);
        }
        return result;
    }

    @Override
    public final long replace(String table, String nullColumnHack, ContentValues initialValues) {
        throwIfNotOpen();
        long t1 = System.currentTimeMillis();
        long result = -1;
        try {
            result = mExecutor.replace(table,nullColumnHack,initialValues);
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage(table, "replace", e, String.valueOf(initialValues));
        }
        if(SLog.isDebug()){
            logDurationMessage(table,"replace", System.currentTimeMillis() - t1);
        }
        return result;
    }

    @Override
    public final Cursor rawQuery(String sql, String[] selectionArgs) {
        throwIfNotOpen();
        long t1 = System.currentTimeMillis();
        Cursor result = null;
        try {
            result = mExecutor.rawQuery(sql,selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage("rawQuery", "rawQuery：", e, String.format("sql = %s，selectionArgs = %s", sql, Arrays.toString(selectionArgs)));
        }
        if(SLog.isDebug()){
            logDurationMessage("unknown","rawQuery："+sql, System.currentTimeMillis() - t1);
        }
        return result;
    }

    @Override
    public final void execSQL(boolean isWrite, String sql, Object[] bindArgs) throws SQLException {
        throwIfNotOpen();
        long t1 = System.currentTimeMillis();
        try {
            mExecutor.execSQL(isWrite,sql,bindArgs);
        } catch (SQLException e) {
            e.printStackTrace();
            logErrorMessage("execSQL", "execSQL", e, String.format("isWrite = %s，sql = %s，bindArgs = %s", isWrite, sql, Arrays.toString(bindArgs)));
            throw e;
        }
        if(SLog.isDebug()){
            logDurationMessage("unknown","isWrite = "+isWrite+"，execSQL："+sql, System.currentTimeMillis() - t1);
        }
    }

    @Override
    public final <T> T executeTransaction(boolean isWrite, SQLiteRunnable<T> r) {
        throwIfNotOpen();
        long t1 = System.currentTimeMillis();
        T result = null;
        try{
            result = mExecutor.executeTransaction(isWrite,r);
        }catch (Exception e){
            e.printStackTrace();
            logErrorMessage("executeTransaction", "executeTransaction", e, String.format("isWrite = %s", isWrite));
        }
        if(SLog.isDebug()){
            logDurationMessage("unknown","isWrite = "+isWrite+"，executeTransaction", System.currentTimeMillis() - t1);
        }
        return result;
    }

    private void logErrorMessage(String table, String method, Exception e, String msg) {
        SLog.e(TAG, String.format("%s[%s]：%s->[%s]，Exception：%s[%s]，msg = %s",mExecutor.getClass().getSimpleName(),databaseName(),table, method, e.getClass(), e.getMessage(), msg));
    }

    private void logDurationMessage(String table, String method, long duration){
        SLog.d(TAG, String.format("%s[%s]：[%s]->[%s]，duration = %sms",mExecutor.getClass().getSimpleName(),databaseName(),table,method,duration));
    }
}
