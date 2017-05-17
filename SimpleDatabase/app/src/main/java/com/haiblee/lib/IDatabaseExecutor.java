package com.haiblee.lib;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Haibiao.Li on 2017/4/24 0024 15:04.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public interface IDatabaseExecutor {
    void close();
    boolean isOpen();
    long insert(String table, String nullColumnHack, ContentValues values);
    int delete(String table, String whereClause, String[] whereArgs);
    int update(String table, ContentValues values, String whereClause, String[] whereArgs);
    long replace(String table, String nullColumnHack, ContentValues initialValues);
    Cursor rawQuery(String sql, String[] selectionArgs);
    void execSQL(boolean isWrite, String sql, Object[] bindArgs) throws SQLException;
    /**如果已经激活WAL，开始事务时，应当使用{@link SQLiteDatabase#beginTransactionNonExclusive()}*/
    <T> T executeTransaction(final boolean isWrite, SQLiteRunnable<T> r);
}
