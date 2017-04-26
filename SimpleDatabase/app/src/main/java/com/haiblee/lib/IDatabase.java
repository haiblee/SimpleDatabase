package com.haiblee.lib;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

/**
 * Created by Haibiao.Li on 2017/4/24 0024 15:04.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public interface IDatabase {
    long insert(String table, String nullColumnHack, ContentValues values);
    int delete(String table, String whereClause, String[] whereArgs);
    int update(String table, ContentValues values, String whereClause, String[] whereArgs);
    long replace(String table, String nullColumnHack, ContentValues initialValues);
    Cursor rawQuery(String sql, String[] selectionArgs);
    void execSQL(boolean isWrite, String sql, Object[] bindArgs) throws SQLException;
    <T> T executeTransaction(final boolean isWrite, SQLiteRunnable<T> r);
}
