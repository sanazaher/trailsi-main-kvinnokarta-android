package com.trailsi.malmo.android.view.activity.aboutus;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trailsi.malmo.android.R;
import com.trailsi.malmo.android.common.cache.CachedData;
import com.trailsi.malmo.android.common.db.DBManager;
import com.trailsi.malmo.android.common.utils.StringHelper;
import com.trailsi.malmo.android.view.activity.base.BaseActivity;

import java.io.File;

public class AboutUsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        initUI();
    }

    private void initUI() {
        setToolBar(getString(R.string.about_us), R.drawable.ic_info_outline, true);

        ImageView imgPlace = findViewById(R.id.img_place);
        String imgPlaceInfo = CachedData.getString(CachedData.kPlaceInfoImage, "");
        File image = DBManager.getInstance().getRealFile(this, imgPlaceInfo);
        if (image != null && image.exists()) {
            Glide.with(this)
                    .load(image)
                    .into(imgPlace);
        }

        TextView txtTitle = findViewById(R.id.txt_title);
        TextView txtDescription = findViewById(R.id.txt_description);
        String title = CachedData.getString(CachedData.kPlaceInfoTitle, "");
        String description = CachedData.getString(CachedData.kPlaceInfoDescription, "");
        txtTitle.setText(StringHelper.getNotNullString(title));
        txtDescription.setText(StringHelper.getNotNullString(description));
    }

    @Override
    public void onBackPressed() {
        Finish();
    }
}