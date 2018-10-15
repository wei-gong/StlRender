package com.gongw.stlrender.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by gw on 2017/6/29.
 */
public class App extends Application {
    private static App app = null;

    @Override
    public void onCreate() {
        super.onCreate();
        if(app == null){
            app = this;
        }
    }

    public static Context getContext(){
        return app.getApplicationContext();
    }
}
