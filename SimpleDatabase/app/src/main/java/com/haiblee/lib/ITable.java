package com.haiblee.lib;

/**
 * Created by Haibiao.Li on 2017/3/15 0015 11:59.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public interface ITable {
    String ROW_ID_NAME = "rowid";

    String tableName();
    String[] createTableSQL();
    String[] alterTableSQL(int version);
}
