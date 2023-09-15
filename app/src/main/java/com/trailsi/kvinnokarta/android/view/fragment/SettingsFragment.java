package com.trailsi.kvinnokarta.android.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.cache.CachedData;
import com.trailsi.kvinnokarta.android.common.db.DBManager;
import com.trailsi.kvinnokarta.android.common.view.RecyclerViewItemClickListener;
import com.trailsi.kvinnokarta.android.model.AudioType;
import com.trailsi.kvinnokarta.android.model.Language;
import com.trailsi.kvinnokarta.android.view.activity.main.MainActivity;
import com.trailsi.kvinnokarta.android.view.adapter.AudioTypeAdapter;
import com.trailsi.kvinnokarta.android.view.adapter.LanguageAdapter;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    public final static String TAG = "SettingsFragment";

    private MainActivity activity;

    private LanguageAdapter languageAdapter;
    private AudioTypeAdapter audioTypeAdapter;

    public SettingsFragment() {
    }

    public SettingsFragment(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        initVariable();
        initUI(view);

        AdView adView= view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return view;
    }

    private void initVariable() {
        if (activity == null) {
            activity = (MainActivity) getActivity();
        }

        List<Language> languages = new ArrayList<>();
        DBManager.getInstance().getLanguages(languages);
        languageAdapter = new LanguageAdapter(activity, languages);

        List<AudioType> types = new ArrayList<>();
        DBManager.getInstance().getAudioTypes(types);
        audioTypeAdapter = new AudioTypeAdapter(activity, types, true);
    }

    private void initUI(View view) {
        ListView lstLanguage = view.findViewById(R.id.lst_language);
        lstLanguage.setAdapter(languageAdapter);
        lstLanguage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Language language = languageAdapter.getItem(position);
                CachedData.setString(CachedData.kLanguage, language.isoCode);
                languageAdapter.notifyDataSetChanged();
            }
        });


        RecyclerView recyclerView = view.findViewById(R.id.lst_audio_type);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(audioTypeAdapter);


        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(),
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        AudioType audioType = audioTypeAdapter.getItem(position);
                        CachedData.setString(CachedData.kAudioType, audioType.name);
                        audioTypeAdapter.notifyDataSetChanged();
                    }
                }));


    }
}
