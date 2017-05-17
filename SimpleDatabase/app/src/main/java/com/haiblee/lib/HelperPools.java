package com.haiblee.lib;

import android.content.Context;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Haibiao.Li on 2017/5/8 0008 16:02.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

class HelperPools {
    private static final String TAG = "HelperPools";
    private final int mMaxSize;
    private final ArrayBlockingQueue<CocoSQLiteOpenHelper> mQueue;
    private final Context context;
    private final AbstractDatabase database;
    private final AtomicInteger mCounter = new AtomicInteger();
    private boolean isDestroy = false;

    public HelperPools(int maxSize, Context context, AbstractDatabase db) {
        mMaxSize = maxSize;
        mQueue = new ArrayBlockingQueue<>(maxSize);
        this.context = context;
        this.database = db;
    }

    public CocoSQLiteOpenHelper obtain() {
        if(isDestroy){
            throw new IllegalStateException("the HelperPools already destroyed,database = "+database.databaseName());
        }
        CocoSQLiteOpenHelper helper = mQueue.peek();
        if(helper == null && mCounter.incrementAndGet() <= mMaxSize){
            helper = new CocoSQLiteOpenHelper(context,database);
            SLog.d(TAG,"HelperPools(db:%s) obtain() , peek return null,new instance,mCounter = %s,mMaxSize = %s",database.hashCode(),mCounter.get(),mMaxSize);
            return helper;
        }else{
            try {
                return mQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int count = mCounter.incrementAndGet();
            SLog.d(TAG, String.format("HelperPools(db:%s) obtain() , take Exception new instance, mCounter = %s,mMaxSize = %s",database.hashCode(),count,mMaxSize));
            return new CocoSQLiteOpenHelper(context,database);
        }
    }

    public boolean recycle(CocoSQLiteOpenHelper helper) {
        if(helper == null){
            return false;
        }
        if(isDestroy){
            try{
                SLog.d(TAG,"recycle()：already destroyed,call helper close()");
                helper.close();
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        boolean success =  mQueue.offer(helper);
        if(!success){
            try{
                helper.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            SLog.w(TAG, String.format("HelperPools(db:%s) recycle queue offer return false ,maybe queue full! mQueue size = %s,mMaxSize = %s",database.databaseName(),mQueue.size(),mMaxSize));
        }
        return success;
    }

    public boolean isDestroy() {
        return isDestroy;
    }

    public synchronized void destroy(){
        SLog.d(TAG,"destroy()：pools size = %s",mQueue.size());
        while (true){
            CocoSQLiteOpenHelper helper = mQueue.poll();
            if(helper == null){
                break;
            }
            try{
                helper.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        isDestroy = true;
    }
}
