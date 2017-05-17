package com.haiblee.example;

import android.content.Context;
import android.util.SparseArray;

import com.haiblee.lib.AbstractDatabase;
import com.haiblee.lib.ITable;

/**
 * Created by Haibiao.Li on 2017/4/26 0026 20:00.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class MyDatabase extends AbstractDatabase {
    public static final String DB_NAME = "game.db";

    public static final int V1_1_0_0 = 1;
    public static final int V2_1_0_1 = 2;
    public static final int DB_VERSION = 2;

    private static MyDatabase INSTANCE = null;

    private MyDatabase(Context context) {
        super(context, DB_NAME,null);
    }

    public static MyDatabase getDefault(){
        if(INSTANCE == null){
            synchronized (MyDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = new MyDatabase(Main.getApplication());
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public SparseArray<ITable[]> staticTables() {
        SparseArray<ITable[]> array = new SparseArray<>(1);
        array.put(V1_1_0_0,new ITable[]{
                new TalkListTable()
        });
//        array.put(V3_1_0_3,new ITable[]{
//                new OtherTable()
//        });
        //...
        return array;
    }

    @Override
    public int databaseVersion() {
        return DB_VERSION;
    }
}
