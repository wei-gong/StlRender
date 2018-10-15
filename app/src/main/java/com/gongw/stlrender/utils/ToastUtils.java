package com.gongw.stlrender.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by gw on 2015/12/26.
 * 统一使用同一个toast对象
 */
public class ToastUtils {
    private static Toast toast;

    /**
     * 显示持续时间短的toast
     * @param context
     * @param message   显示信息
     */
    public static void showShort(Context context, CharSequence message){
        if(toast == null){
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        }else{
            toast.setText(message);
            toast.setDuration( Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    /**
     * 显示持续时间长的toast
     * @param context
     * @param message   显示信息
     */
    public static void showLong(Context context, CharSequence message){
        if(toast == null){
            toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        }else{
            toast.setText(message);
            toast.setDuration( Toast.LENGTH_LONG);
        }
        toast.show();
    }

    public static void hide(){
        if(toast != null){
            toast.cancel();
        }
    }
}
