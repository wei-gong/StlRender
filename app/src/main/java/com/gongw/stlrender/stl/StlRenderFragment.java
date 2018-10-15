package com.gongw.stlrender.stl;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.gongw.stlrender.R;
import com.gongw.stlrender.base.BaseFragment;
import com.gongw.stlrender.utils.ToastUtils;

import java.io.File;
import butterknife.InjectView;

/**
 * Created by gw on 2017/7/11.
 */

public class StlRenderFragment extends BaseFragment implements StlRenderContract.View{
    @InjectView(R.id.tv_back_title)
    TextView tvBackTitle;
    @InjectView(R.id.stlView)
    STLView stlView;
    @InjectView(R.id.tv_x_size)
    TextView tvSizeX;
    @InjectView(R.id.tv_y_size)
    TextView tvSizeY;
    @InjectView(R.id.tv_z_size)
    TextView tvSizeZ;
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
        showBackTitle(getString(R.string.model_render));
        presenter.loadModel(stlFile);
        stlView.setOnStlSizeChangedListener(new STLView.OnStlSizeChangedListener() {
            @Override
            public void onStlSizeChanged(float[] newSize) {
                showSizeX(newSize[0]);
                showSizeY(newSize[1]);
                showSizeZ(newSize[2]);
            }
        });
    }

    public void showBackTitle(String title) {
        tvBackTitle.setText(title);
    }

    @Override
    public void showToastMsg(String msg) {
        ToastUtils.showShort(mContext, msg);
    }

    @Override
    public void showModel(STLObject stlObject) {
        stlView.requestRedraw(stlObject);
        float[] size = stlView.getStlObjectSize();
        showSizeX(size[0]);
        showSizeY(size[1]);
        showSizeZ(size[2]);
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
    public void showSizeX(float x) {
        tvSizeX.setText("X: "+x);
    }

    @Override
    public void showSizeY(float y) {
        tvSizeY.setText("Z: "+y);
    }

    @Override
    public void showSizeZ(float z) {
        tvSizeZ.setText("Z: "+z);
    }

    @Override
    public void showHomePageFragment() {
        ((AppCompatActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getFragmentManager().popBackStack();
            }
        });
    }
}
