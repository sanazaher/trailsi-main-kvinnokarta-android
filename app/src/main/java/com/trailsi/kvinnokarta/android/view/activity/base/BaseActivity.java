package com.trailsi.kvinnokarta.android.view.activity.base;


import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.cache.CachedData;
import com.trailsi.kvinnokarta.android.common.utils.StringHelper;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String TAG = "BaseActivity";

    private KProgressHUD mProgressDialog;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setLanguage() {
        String lang = CachedData.getString(CachedData.kLanguage, "");
        if (StringHelper.isEmpty(lang)) {
            return;
        }
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public void setToolBar(String title, int iconResourceId, boolean goBack) {
        // Set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(goBack);
            actionBar.setTitle("");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView txtTitle = toolbar.findViewById(R.id.txt_actionbar_title);
        if (txtTitle != null) {
            txtTitle.setText(title);
        }

        ImageView imgIcon = toolbar.findViewById(R.id.img_icon);
        if (imgIcon != null && iconResourceId > 0) {
            imgIcon.setImageResource(iconResourceId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    synchronized public void showProgressDialog() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel(getString(R.string.please_wait))
                    .setDetailsLabel(getString(R.string.downloading_content))
                    .setCancellable(false)
                    .setBackgroundColor(getResources().getColor(R.color.color_black))
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show();
        }
    }

    synchronized public void showDownloadProgressAlert(int progress) {
        String message = getString(R.string.downloading_content) + ": " + progress + "%";
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            mProgressDialog = KProgressHUD.create(this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel(getString(R.string.please_wait))
                    .setDetailsLabel(message)
                    .setCancellable(false)
                    .setBackgroundColor(getResources().getColor(R.color.color_black))
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show();
        } else {
            mProgressDialog.setDetailsLabel(message);
        }
    }

    synchronized public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void Finish() {
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
