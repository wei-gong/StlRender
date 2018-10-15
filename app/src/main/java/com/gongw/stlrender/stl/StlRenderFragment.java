package com.gongw.stlrender.stl;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import com.gongw.stlrender.R;
import com.gongw.stlrender.base.BaseFragment;
import com.gongw.stlrender.utils.ToastUtils;
import java.io.File;
import butterknife.InjectView;

/**
 * Created by gw on 2017/7/11.
 */

public class StlRenderFragment extends BaseFragment implements StlRenderContract.View{
    @InjectView(R.id.stlView)
    STLView stlView;
    File stlFile;
    StlRenderContract.Presenter presenter;

    public static StlRenderFragment getInstance(File file){
        StlRenderFragment fragment = new StlRenderFragment();
        fragment.stlFile = file;
        fragment.presenter = new StlRenderPresenter(fragment);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_stl_render;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        presenter.loadModel(stlFile);
    }

    @Override
    public void showToastMsg(String msg) {
        ToastUtils.showShort(mContext, msg);
    }

    @Override
    public void showModel(STLObject stlObject) {
        stlView.getStlRenderer().requestRedraw(stlObject);
    }

    ProgressDialog stlProgressDialog;
    @Override
    public void showFetchProgressDialog(int progress) {
        if(stlProgressDialog == null){
            stlProgressDialog = new ProgressDialog(mContext);
        }
        stlProgressDialog.setTitle(R.string.stl_load_progress_title);
        stlProgressDialog.setIndeterminate(false);
        stlProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        stlProgressDialog.setMax(100);
        stlProgressDialog.setCancelable(false);
        stlProgressDialog.setProgress(progress);
        stlProgressDialog.show();
    }

    @Override
    public void hideFetchProgressDialog() {
        if(stlProgressDialog != null){
            stlProgressDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stlView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        stlView.onResume();
        stlView.getStlRenderer().requestRedraw();
    }
}
