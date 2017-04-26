package com.haiblee.lib;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Haibiao.Li on 2017/4/20 0020 18:46.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public abstract class SQLiteRunnable<T> {

    public abstract T run(SQLiteDatabase db);
}
