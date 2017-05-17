package com.haiblee.example;


import com.haiblee.lib.Column;
import com.haiblee.lib.ITable;
import com.haiblee.lib.SQLCreator;
import com.haiblee.lib.TableField;

/**
 * Created by Haibiao.Li on 2017/4/20 0020 18:59.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class TalkListTable implements ITable {

    public static final String TABLE_NAME = "talk_list";

    public static final Column TARGET_ID = new Column(0, "target_id");
    public static final Column MESSAGE_ID = new Column(1, "message_id");
    public static final Column TYPE = new Column(2, "type");
    public static final Column PRIORITY = new Column(3, "priority");
    public static final Column REMIND_MODE = new Column(4, "remind_mode");
    public static final Column UNREAD = new Column(5,"unread");
    public static final Column BRIEFLY = new Column(6,"briefly");
    public static final Column TALK_GROUP = new Column(7,"talk_group");
    public static final Column EXTRA_FLAG = new Column(8, "extra_flag");
    public static final Column UPDATE_TIME = new Column(9, "update_time");

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String[] createTableSQL() {
        TableField targetId = new TableField(TARGET_ID.name, TableField.FLAG_NOT_NULL | TableField.FLAG_UNIQUE | TableField.FLAG_INDEX_KEY);
        return new String[]{
                SQLCreator.createTableSQL(TABLE_NAME, new TableField[]{
                        targetId,
                        new TableField(MESSAGE_ID.name, TableField.FLAG_NOT_NULL),
                        new TableField(TYPE.name, TableField.Typed.INTEGER),
                        new TableField(PRIORITY.name, TableField.Typed.INTEGER),
                        new TableField(REMIND_MODE.name, TableField.Typed.INTEGER),
                        new TableField(UNREAD.name, TableField.Typed.INTEGER),
                        new TableField(BRIEFLY.name, TableField.Typed.TEXT),
                        new TableField(TALK_GROUP.name, TableField.Typed.INTEGER),
                        new TableField(EXTRA_FLAG.name, TableField.Typed.INTEGER),
                        new TableField(UPDATE_TIME.name, TableField.Typed.INTEGER),
                }),
                SQLCreator.createIndex(TABLE_NAME,targetId)
        };
    }

    @Override
    public String[] alterTableSQL(int version) {
        String[] result = null;
        switch (version) {
            case MyDatabase.V2_1_0_1:
                result = new String[]{
                        SQLCreator.addTableField(TABLE_NAME,new TableField("new_field"))
                };
                break;
//            case MyDatabase.V3_1_0_3:
//                result = new String[]{
//                        SQLCreator.addTableFild(TABLE_NAME,new TableField("new_field"))
//                };
//                break;
            default:
                result = new String[0];
        }
        return result;
    }
}
