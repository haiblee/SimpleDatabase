package com.haiblee.lib;


/**
 * Created by Haibiao.Li on 2016/12/13 0013 15:44.
 * <br>Email:lihaibiaowork@gmail.com</br>
 * <p><b>注释：</b></p>
 */

public class SLog {

    private static Boolean isDebug = true;

    public static boolean isDebug(){
//        if(isDebug == null){
//            ApplicationInfo info = LibraryApplet.getContext().getApplicationInfo();
//            isDebug = (info != null && ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0));
//        }
        return isDebug;
    }

    public static void v(String tag, String message) {
        android.util.Log.v(tag,message);
    }

    public static void d(String tag, String message) {
        android.util.Log.d(tag,message);
    }

    public static void i(String tag, String message) {
        android.util.Log.i(tag,message);
    }

    public static void w(String tag, String message) {
        android.util.Log.w(tag,message);
    }

    public static void e(String tag, String message) {
        android.util.Log.e(tag,message);
    }

    public static void e(String tag, Throwable throwable) {
        android.util.Log.e(tag, throwable != null ? throwable.getMessage() : "", throwable);
    }

    public static void e(String tag, String msg, Throwable t) {
        android.util.Log.e(tag, msg, t);
    }

    public static void v(Object obj, String message) {
        String myTag = tag(obj);
        android.util.Log.v(myTag,obj + ":" + message);
    }

    public static void d(Object obj, String message) {
        String myTag = tag(obj);

        android.util.Log.d(myTag,obj + ":" + message);
    }

    public static void i(Object obj, String message) {
        String myTag = tag(obj);
        android.util.Log.i(myTag, obj + ":" + message);
    }

    public static void w(Object obj, String message) {
        String myTag = tag(obj);
        android.util.Log.w(myTag,obj + ":" + message);
    }

    public static void e(Object obj, String message) {
        String myTag = tag(obj);
        android.util.Log.e(myTag,obj + ":" + message);
    }

    public static void v(Object obj, String msgFormat, Object... args) {
        try {
            String msg = String.format(msgFormat, args);
            //v(obj,msg);
            //为保持调用栈层级一致，直接调用log函数而不是上层封装
            String myTag = tag(obj);
            android.util.Log.v(myTag,obj + ":" + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void d(Object obj, String msgFormat, Object... args) {
        try {
            String msg = String.format(msgFormat, args);
            //d(obj,msg);
            String myTag = tag(obj);
            android.util.Log.d(myTag,obj + ":" + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void i(Object obj, String msgFormat, Object... args) {
        try {
            String msg = String.format(msgFormat, args);
            //i(obj,msg);
            String myTag = tag(obj);
            android.util.Log.i(myTag,obj + ":" + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void w(Object obj, String msgFormat, Object... args) {
        try {
            String msg = String.format(msgFormat, args);
            //w(obj,msg);
            String myTag = tag(obj);
            android.util.Log.w(myTag,obj + ":" + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void e(Object obj, String msgFormat, Object... args) {
        try {
            String msg = String.format(msgFormat, args);
            //e(obj,msg);
            String myTag = tag(obj);
            android.util.Log.e(myTag,obj + ":" + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String tag(Object tag) {
        return tag instanceof String ? (String) tag
                : tag.getClass().getSimpleName();
    }

}
