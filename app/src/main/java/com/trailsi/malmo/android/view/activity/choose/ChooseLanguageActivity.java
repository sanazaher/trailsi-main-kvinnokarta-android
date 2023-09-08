package com.trailsi.malmo.android.view.activity.choose;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.trailsi.malmo.android.R;
import com.trailsi.malmo.android.common.cache.CachedData;
import com.trailsi.malmo.android.common.db.DBManager;
import com.trailsi.malmo.android.model.Language;
import com.trailsi.malmo.android.view.activity.base.BaseActivity;
import com.trailsi.malmo.android.view.adapter.LanguageAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChooseLanguageActivity extends BaseActivity {

    private LanguageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);

        initVariable();
        initUI();
    }

    private void initVariable() {
        List<Language> languages = new ArrayList<>();
        DBManager.getInstance().getLanguages(languages);
        mAdapter = new LanguageAdapter(this, languages);
    }

    private void initUI() {
        setToolBar(getString(R.string.language), R.drawable.ic_language, false);

        ListView lstLanguage = findViewById(R.id.lst_language);
        lstLanguage.setAdapter(mAdapter);
        lstLanguage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Language language = mAdapter.getItem(position);
                gotoChooseAudioTrack(language.isoCode);
            }
        });
    }

    private void gotoChooseAudioTrack(String lang) {
        CachedData.setString(CachedData.kLanguage, lang);

        Intent intent = new Intent(this, ChooseAudioTourActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override
    public void onBackPressed() {
        Finish();
    }
}
