package com.trailsi.kvinnokarta.android.view.activity.splash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.Constant;
import com.trailsi.kvinnokarta.android.common.cache.CachedData;
import com.trailsi.kvinnokarta.android.common.notify.NotifyAccept;
import com.trailsi.kvinnokarta.android.common.utils.DialogUtil;
import com.trailsi.kvinnokarta.android.common.utils.StringHelper;
import com.trailsi.kvinnokarta.android.controller.api.ApiClient;
import com.trailsi.kvinnokarta.android.view.activity.base.BaseActivity;
import com.trailsi.kvinnokarta.android.view.activity.choose.ChooseAudioTourActivity;
import com.trailsi.kvinnokarta.android.view.activity.choose.ChooseLanguageActivity;
import com.trailsi.kvinnokarta.android.view.activity.main.MainActivity;

public class SplashScreenActivity extends BaseActivity {

    private long T0, T1;

    private static final long waiting = 500;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        checkStoragePermission();
    }

    @Override
    public void onBackPressed() {
    }

    private void progressBar(){
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);

                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private void checkStoragePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            DialogUtil.ShowAlert(this,
                    getString(R.string.storage_permission_required),
                    getString(R.string.storage_permission_required_msg),
                    new NotifyAccept() {
                        @Override
                        public void onAccept(Object object) {
                            ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{permission}, Constant.REQUEST_STORAGE_PERMISSION);
                        }
                    });
        } else {
            checkLocationPermission();
        }
    }

    private void checkLocationPermission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            DialogUtil.ShowAlert(this,
                    getString(R.string.location_permission_required),
                    getString(R.string.location_permission_required_msg),
                    new NotifyAccept() {
                        @Override
                        public void onAccept(Object object) {
                            ActivityCompat.requestPermissions(SplashScreenActivity.this, new String[]{permission}, Constant.REQUEST_LOCATION_PERMISSION);
                        }
                    });
        } else {
            onPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String permission;
        boolean isGranted;
        int numOfRequest = grantResults.length;
        isGranted = numOfRequest == 1 && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
        switch (requestCode) {
            case Constant.REQUEST_STORAGE_PERMISSION:
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                if (isGranted) {
                    checkLocationPermission();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        checkStoragePermission();
                    } else {
                        showOpenPermissionSetting(Constant.REQUEST_STORAGE_PERMISSION);
                    }
                }
                break;

            case Constant.REQUEST_LOCATION_PERMISSION:
                permission = Manifest.permission.ACCESS_FINE_LOCATION;
                if (isGranted) {
                    onPermissionGranted();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        checkLocationPermission();
                    } else {
                        showOpenPermissionSetting(Constant.REQUEST_LOCATION_PERMISSION);
                    }
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showOpenPermissionSetting(int request) {
        String title = "";
        String message = "";
        if (request == Constant.REQUEST_LOCATION_PERMISSION) {
            title = getString(R.string.location_permission_required);
            message = getString(R.string.location_permission_required_msg);
        } else {
            title = getString(R.string.storage_permission_required);
            message = getString(R.string.storage_permission_required_msg);
        }
        DialogUtil.ShowAlert(this, title, message, new NotifyAccept() {
            @Override
            public void onAccept(Object object) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Constant.REQUEST_PERMISSION_SETTING);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_PERMISSION_SETTING:
                checkStoragePermission();
                break;
            case Constant.REQUEST_WIFI_SETTING:
                downloadPlaceInfo();
                break;
        }
    }

    private void onPermissionGranted() {

        progressBar();
        downloadPlaceInfo();
    }

    private void downloadPlaceInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int retry = 0;
                boolean isFetched = false;
                while (retry < 2) {
                    isFetched = ApiClient.downloadPlaceInfo();
                    if (isFetched) {
                        retry = 2;
                    }
                    retry++;
                }

                T0 = System.currentTimeMillis();
                try {
                    T1 = System.currentTimeMillis() - T0;
                    if (T1 < waiting)
                        Thread.sleep(waiting - T1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean isPlaceInfoDownloaded = CachedData.getBoolean(CachedData.kPlaceInfoDownloaded, false);
                if (isFetched || isPlaceInfoDownloaded) {
                    gotoNextActivity();
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDownloadFailedDialog();
                    }
                });
            }
        }).start();
    }

    private void showDownloadFailedDialog() {
        if (!StringHelper.isEmpty(CachedData.getString(CachedData.kPlaceUUID, ""))) {
            gotoNextActivity();
            return;
        }

        LayoutInflater li = LayoutInflater.from(SplashScreenActivity.this);
        View promptsView = li.inflate(R.layout.dlg_confirm, null);
        final AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(SplashScreenActivity.this, R.style.AppTheme_Dialog))
                .setView(promptsView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView txtTitle = promptsView.findViewById(R.id.txt_title);
        TextView txtMsg = promptsView.findViewById(R.id.txt_msg);
        txtTitle.setText(getString(R.string.download_failed));
        txtMsg.setText(getString(R.string.desc_check_internet));

        Button btnOK = promptsView.findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    Intent intentWifi = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                    startActivityForResult(intentWifi, Constant.REQUEST_WIFI_SETTING);
                } catch (Exception e) {
                    e.printStackTrace();
                    Finish();
                }
            }
        });

        Button btnCancel = promptsView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Finish();
            }
        });
        dialog.show();
    }

    private void gotoNextActivity() {
        String language = CachedData.getString(CachedData.kLanguage, "");
        if (StringHelper.isEmpty(language)) {
            gotoChooseLanguage();
            return;
        }

        String audioType = CachedData.getString(CachedData.kAudioType, "");
        if (StringHelper.isEmpty(audioType)) {
            gotoChooseAudioTrack();
            return;
        }

        gotoMainActivity();
    }

    private void gotoChooseLanguage() {
        Intent intent = new Intent(this, ChooseLanguageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }

    private void gotoChooseAudioTrack() {
        Intent intent = new Intent(this, ChooseAudioTourActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }
}
