package com.haiblee.example;

import android.app.Application;

/**
 * Created by Haibiao.Li on 2017/4/26 0026 20:05.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class Main {

    public static Application getApplication(){
        return null;//TODO return application instance
    }

    public Main() {
        MyDatabase db = MyDatabase.getDefault();
        for(int i = 0 ; i < 10 ; i++){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    //safe
                    //db.insert()
                    //db.delete();
                    //db.update();
                    //db.rawQuery();
                    //db.replace();
                    //db.execSQL();
                    //db.executeTransaction();
                }
            }.start();
        }

    }
}
