package com.haiblee.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Haibiao.Li on 2017/5/8 0008 15:39.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class LockDatabaseExecutor implements IDatabaseExecutor {
    private static final String TAG = "LockDatabaseExecutor";
    private final AbstractDatabase mDatabase;
    private final HelperPools mHelperPools;
    private final ReadWriteLock mRwLock = new ReentrantReadWriteLock(true);
    private volatile boolean isOpen = false;

    public LockDatabaseExecutor(Context context, AbstractDatabase database) {
        mDatabase = database;
        this.mHelperPools = new HelperPools(5, context, database);
        isOpen = true;
    }

    private Lock doLock(boolean isWriteLock) {
        final Lock lock = isWriteLock ? mRwLock.writeLock() : mRwLock.readLock();
        lock.lock();
        return lock;
    }

    private void unLock(Lock lock) {
        lock.unlock();
    }

    @Override
    public synchronized void close() {
        mHelperPools.destroy();
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

    @Override
    public final long insert(String table, String nullColumnHack, ContentValues values) {
        throwIfNotOpen();
        Lock lock = doLock(true);
        CocoSQLiteOpenHelper helper = null;
        try {
            helper = mHelperPools.obtain();
            return helper.getWritableDatabase().insert(table, nullColumnHack, values);
        } finally {
            mHelperPools.recycle(helper);
            unLock(lock);
        }
    }

    @Override
    public final int delete(String table, String whereClause, String[] whereArgs) {
        throwIfNotOpen();
        Lock lock = doLock(true);
        CocoSQLiteOpenHelper helper = null;
        try {
            helper = mHelperPools.obtain();
            int result = helper.getWritableDatabase().delete(table, whereClause, whereArgs);
            return result;
        }finally {
            mHelperPools.recycle(helper);
            unLock(lock);
        }
    }

    @Override
    public final int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        throwIfNotOpen();
        Lock lock = doLock(true);
        CocoSQLiteOpenHelper helper = null;
        try {
            helper = mHelperPools.obtain();
            int result = helper.getWritableDatabase().update(table, values, whereClause, whereArgs);
            return result;
        } finally {
            mHelperPools.recycle(helper);
            unLock(lock);
        }
    }

    @Override
    public final long replace(String table, String nullColumnHack, ContentValues initialValues) {
        throwIfNotOpen();
        Lock lock = doLock(true);
        CocoSQLiteOpenHelper helper = null;
        try {
            helper = mHelperPools.obtain();
            long result = helper.getWritableDatabase().replace(table, nullColumnHack, initialValues);
            return result;
        }finally {
            mHelperPools.recycle(helper);
            unLock(lock);
        }
    }

    @Override
    public final Cursor rawQuery(String sql, String[] selectionArgs) {
        throwIfNotOpen();
        Lock lock = doLock(false);
        CocoSQLiteOpenHelper helper = null;
        try {
            helper = mHelperPools.obtain();
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor result = db.rawQuery(sql, selectionArgs);
            return result;
        } finally {
            mHelperPools.recycle(helper);
            unLock(lock);
        }
    }

    @Override
    public final void execSQL(boolean isWrite, String sql, Object[] bindArgs) throws SQLException {
        throwIfNotOpen();
        Lock lock = doLock(isWrite);
        CocoSQLiteOpenHelper helper = null;
        try {
            helper = mHelperPools.obtain();
            SQLiteDatabase db = isWrite ? helper.getWritableDatabase() : helper.getReadableDatabase();
            db.execSQL(sql, bindArgs);
        } finally {
            mHelperPools.recycle(helper);
            unLock(lock);
        }
    }

    @Override
    public final <T> T executeTransaction(final boolean isWrite, SQLiteRunnable<T> r) {
        throwIfNotOpen();
        Lock lock = doLock(isWrite);
        CocoSQLiteOpenHelper helper = null;
        try {
            helper = mHelperPools.obtain();
            SQLiteDatabase db = isWrite ? helper.getWritableDatabase() : helper.getReadableDatabase();
            T result = r.run(db);
            if (!db.isOpen()) {
                throw new UnsupportedOperationException(mDatabase.databaseName() + " executeTransaction SQLiteRunnable run not allow call SQLiteDatabase.close()");
            }
            return result;
        }finally {
            mHelperPools.recycle(helper);
            unLock(lock);
        }
    }
}
