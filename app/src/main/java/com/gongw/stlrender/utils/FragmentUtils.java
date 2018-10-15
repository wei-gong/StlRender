package com.gongw.stlrender.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by gw on 2016/7/1.
 */

public class FragmentUtils {

    public static void addFragment(FragmentManager fragmentManager, Fragment fragment, int frameId, boolean addToBackStacks){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment, fragment.getClass().getSimpleName());
        if(addToBackStacks) transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.commitAllowingStateLoss();
    }

    public static void replaceFragment(FragmentManager fragmentManager, Fragment fragment, int frameId, boolean addToBackStacks){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment);
        if(addToBackStacks) transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

}
