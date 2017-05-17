package com.haiblee.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Haibiao.Li on 2017/5/8 0008 17:36.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class WALDatabaseExecutor implements IDatabaseExecutor {
    private static final String TAG = "WALDatabaseExecutor";
    private final CocoSQLiteOpenHelper mHelper;
    private volatile boolean isOpen = false;

    public WALDatabaseExecutor(Context context, AbstractDatabase database) {
        mHelper = new CocoSQLiteOpenHelper(context,database);
        isOpen = true;
    }

    @Override
    public synchronized void close() {
        mHelper.close();
        isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return mHelper.getWritableDatabase().insert(table,nullColumnHack,values);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return mHelper.getWritableDatabase().delete(table,whereClause,whereArgs);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return  mHelper.getWritableDatabase().update(table,values,whereClause,whereArgs);
    }

    @Override
    public long replace(String table, String nullColumnHack, ContentValues initialValues) {
        return  mHelper.getWritableDatabase().replace(table,nullColumnHack,initialValues);
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return  mHelper.getReadableDatabase().rawQuery(sql,selectionArgs);
    }

    @Override
    public void execSQL(boolean isWrite, String sql, Object[] bindArgs) throws SQLException {
        SQLiteDatabase db = isWrite ? mHelper.getWritableDatabase() : mHelper.getReadableDatabase();
        db.execSQL(sql,bindArgs);
    }

    @Override
    public <T> T executeTransaction(boolean isWrite, SQLiteRunnable<T> r) {
        SQLiteDatabase db = isWrite ? mHelper.getWritableDatabase() : mHelper.getReadableDatabase();
        return r.run(db);
    }
}
