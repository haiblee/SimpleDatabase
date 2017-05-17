package com.haiblee.lib;

/**
 * Created by Haibiao.Li on 2017/4/21 0021 16:16.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class Column {
    public final int index;
    public final String name;

    public Column(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public static Column create(int index,String name){
        return new Column(index,name);
    }

    @Override
    public String toString() {
        return name;
    }
}
