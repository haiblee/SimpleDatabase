package com.haiblee.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.Pools;
import android.util.Log;
import android.util.SparseArray;


import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Haibiao.Li on 2017/4/20 0020 15:10.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 * 抽象数据库，允许同时多线程读，但同时只允许一个线程写，且读写互斥
 */

public abstract class AbstractDatabase implements IDatabase {
    private static final String TAG = "AbstractDatabase";

    public static final int POOL_SIZE_DEFAULT = 5;
    private final Context mContext;
    private final ReadWriteLock mRwLock = new ReentrantReadWriteLock();
    private final HelperPools mHelperPools;

    public AbstractDatabase(Context context) {
        this(context, POOL_SIZE_DEFAULT);
    }

    /**
     * 创建一个数据库
     * @param context context
     * @param poolSize 连接池的大小，可以理解成最多允许poolSize个线程读数据库
     */
    public AbstractDatabase(Context context, int poolSize) {
        mContext = context;
        mHelperPools = new HelperPools(poolSize, context, this);
    }

    public String storagePath() {
        return mContext.getDatabasePath(databaseName()).getPath();
    }

    public abstract String databaseName();

    public abstract SparseArray<ITable[]> staticTables();

    public abstract int databaseVersion();

    @Override
    public final long insert(String table, String nullColumnHack, ContentValues values) {
        mRwLock.writeLock().lock();
        try {
            CocoSQLiteOpenHelper helper = mHelperPools.take();
            long result = helper.getWritableDatabase().insert(table, nullColumnHack, values);
            mHelperPools.recycle(helper);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage(table, "insert", e, String.valueOf(values));
        } finally {
            mRwLock.writeLock().unlock();
        }
        return -1;
    }

    @Override
    public final int delete(String table, String whereClause, String[] whereArgs) {
        mRwLock.writeLock().lock();
        try {
            CocoSQLiteOpenHelper helper = mHelperPools.take();
            int result = helper.getWritableDatabase().delete(table, whereClause, whereArgs);
            mHelperPools.recycle(helper);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage(table, "delete", e, String.format("whereClause = %s，whereArgs = %s", whereClause, Arrays.toString(whereArgs)));
        } finally {
            mRwLock.writeLock().unlock();
        }
        return -1;
    }

    @Override
    public final int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        mRwLock.writeLock().lock();
        try {
            CocoSQLiteOpenHelper helper = mHelperPools.take();
            int result = helper.getWritableDatabase().update(table, values, whereClause, whereArgs);
            mHelperPools.recycle(helper);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage(table, "update", e, String.format("ContentValues = %s，whereClause = %s，whereArgs = %s", String.valueOf(values), whereClause, Arrays.toString(whereArgs)));
        } finally {
            mRwLock.writeLock().unlock();
        }
        return -1;
    }

    @Override
    public final long replace(String table, String nullColumnHack, ContentValues initialValues) {
        mRwLock.writeLock().lock();
        try {
            CocoSQLiteOpenHelper helper = mHelperPools.take();
            long result = helper.getWritableDatabase().replace(table, nullColumnHack, initialValues);
            mHelperPools.recycle(helper);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage(table, "replace", e, String.valueOf(initialValues));
        } finally {
            mRwLock.writeLock().unlock();
        }
        return -1;
    }

    @Override
    public final Cursor rawQuery(String sql, String[] selectionArgs) {
        mRwLock.readLock().lock();
        try {
            CocoSQLiteOpenHelper helper = mHelperPools.take();
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor result = db.rawQuery(sql, selectionArgs);
            mHelperPools.recycle(helper);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage("rawQuery", "rawQuery", e, String.format("sql = %s，selectionArgs = %s", sql, Arrays.toString(selectionArgs)));
        } finally {
            mRwLock.readLock().unlock();
        }
        return null;
    }

    @Override
    public final void execSQL(boolean isWrite, String sql, Object[] bindArgs) throws SQLException {
        final Lock lock = isWrite ? mRwLock.writeLock() : mRwLock.readLock();
        lock.lock();
        try {
            CocoSQLiteOpenHelper helper = mHelperPools.take();
            SQLiteDatabase db = isWrite ? helper.getWritableDatabase() : helper.getReadableDatabase();
            db.execSQL(sql, bindArgs);
            mHelperPools.recycle(helper);
        } catch (SQLException e) {
            e.printStackTrace();
            logErrorMessage("execSQL","execSQL",e,String.format("isWrite = %s，sql = %s，bindArgs = %s",isWrite,sql,Arrays.toString(bindArgs)));
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public final <T> T executeTransaction(final boolean isWrite, SQLiteRunnable<T> r) {
        final Lock lock = isWrite ? mRwLock.writeLock() : mRwLock.readLock();
        lock.lock();
        try {
            CocoSQLiteOpenHelper helper = mHelperPools.take();
            SQLiteDatabase db = isWrite ? helper.getWritableDatabase() : helper.getReadableDatabase();
            T result = r.run(db);
            if(!db.isOpen()){
                throw new UnsupportedOperationException(databaseName() + " executeTransaction SQLiteRunnable run not allow call SQLiteDatabase.close()");
            }
            mHelperPools.recycle(helper);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logErrorMessage("executeTransaction", "executeTransaction", e, String.format("isWrite = %s", isWrite));
        } finally {
            lock.unlock();
        }
        return null;
    }

    private void logErrorMessage(String table, String method, Exception e, String msg) {
        Log.e(TAG, String.format("databaseName = %s：table = %s->method = %s，Exception：%s[%s]，msg = %s", databaseName(), table, method, e.getClass(), e.getMessage(), msg));
    }

    private static class HelperPools{
        private static final String TAG = "HelperPools";
        private final int mMaxPoolSize;
        private final Pools.SimplePool<CocoSQLiteOpenHelper> mPools;
        private final Context context;
        private final AbstractDatabase databaseProxy;
        private final Object mLock = new Object();
        private volatile int mInstanceCount = 0;

        public HelperPools(int maxPoolSize, Context context, AbstractDatabase proxy) {
            this.mMaxPoolSize = maxPoolSize;
            mPools = new Pools.SimplePool<>(maxPoolSize);
            this.context = context;
            this.databaseProxy = proxy;
        }

        /**
         * 从池中获取一个对象，如果超出最大数量限制，则阻塞
         * @return 取回结果
         */
        public CocoSQLiteOpenHelper take() {
            synchronized (mLock){
                while (mInstanceCount >= mMaxPoolSize) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG,"take()，InterruptedException ："+e.getMessage());
                    }
                }
                CocoSQLiteOpenHelper instance = mPools.acquire();
                if (instance == null) {
                    instance = new CocoSQLiteOpenHelper(context, databaseProxy);
                }
                mInstanceCount++;
                //Log.d(TAG,"obtain(),sInstanceCount = "+mInstanceCount+",MAX_POOL_SIZE = "+mMaxPoolSize);
                return instance;
            }
        }

        /***
         * 回收对象到对象池
         * @param helper 待回收的对象
         * @return 成功true
         */
        public boolean recycle(CocoSQLiteOpenHelper helper) {
            synchronized (mLock){
                if (mPools.release(helper)) {
                    mInstanceCount--;
                    mLock.notify();
                    //Log.d(TAG,"recycle(),sInstanceCount = "+sInstanceCount+",MAX_POOL_SIZE = "+MAX_POOL_SIZE);
                    return true;
                }
                return false;
            }
        }
    }
}
