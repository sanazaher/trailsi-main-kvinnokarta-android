package com.trailsi.kvinnokarta.android.view.activity.choose;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.cache.CachedData;
import com.trailsi.kvinnokarta.android.common.db.DBManager;
import com.trailsi.kvinnokarta.android.common.view.RecyclerViewItemClickListener;
import com.trailsi.kvinnokarta.android.model.AudioType;
import com.trailsi.kvinnokarta.android.view.activity.base.BaseActivity;
import com.trailsi.kvinnokarta.android.view.activity.main.MainActivity;
import com.trailsi.kvinnokarta.android.view.adapter.AudioTypeAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChooseAudioTourActivity extends BaseActivity {

    private AudioTypeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_audio_tour);

        initVariable();
        initUI();
    }

    private void initVariable() {
        List<AudioType> types = new ArrayList<>();
        DBManager.getInstance().getAudioTypes(types);
        mAdapter = new AudioTypeAdapter(this, types, false);
    }

    private void initUI() {
        setToolBar(getString(R.string.audio_track), R.drawable.ic_audio, false);

        RecyclerView recyclerView = findViewById(R.id.lst_audio_type);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                AudioType audioType = mAdapter.getItem(position);
                gotoMain(audioType.name);
            }
        }));
    }

    private void gotoMain(String type) {
        CachedData.setString(CachedData.kAudioType, type);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void onBackPressed() {
        Finish();
    }
}
