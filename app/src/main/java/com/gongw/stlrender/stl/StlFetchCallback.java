package com.gongw.stlrender.stl;

/**
 * Created by gw on 2016/6/29.
 */

public interface StlFetchCallback {
    void onBefore();
    void onProgress(int progress);
    void onFinish(STLObject stlObject);
    void onError();
}
