package com.gongw.stlrender.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.gongw.stlrender.R;
import com.gongw.stlrender.base.BaseFragment;
import com.gongw.stlrender.stl.StlRenderFragment;
import com.gongw.stlrender.utils.FileUtils;
import com.gongw.stlrender.utils.FragmentUtils;
import java.io.File;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by gongw on 2018/10/15.
 */

public class HomeFragment extends BaseFragment {

    @InjectView(R.id.btn_choose_stl)
    Button chooseStlBtn;
    public static final int REQUEST_STL_FILE = 0X01;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {

    }

    @OnClick({R.id.btn_choose_stl})
    void chooseFIle(View view){
        switch (view.getId()){
            case R.id.btn_choose_stl:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.stl_render)), REQUEST_STL_FILE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            File file = FileUtils.getFileByUri(data.getData(), mContext);
            switch (requestCode){
                case REQUEST_STL_FILE:
                    //打开STL渲染界面
                    FragmentUtils.addFragment(getFragmentManager(), StlRenderFragment.getInstance(file), R.id.fl_container, true);
                    break;

                default:
                    break;
            }
        }
    }

    }
