package com.gongw.stlrender.home;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gongw.stlrender.R;
import com.gongw.stlrender.utils.FragmentUtils;

/**
 * Created by gongw on 2018/10/15.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentUtils.addFragment(getSupportFragmentManager(), new HomeFragment(), R.id.fl_container, false);
    }
}
