package com.trailsi.malmo.android.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.trailsi.malmo.android.R;
import com.trailsi.malmo.android.common.db.DBManager;
import com.trailsi.malmo.android.common.utils.StringHelper;
import com.trailsi.malmo.android.model.Document;
import com.trailsi.malmo.android.view.activity.main.MainActivity;
import com.trailsi.malmo.android.view.adapter.DownloadAdapter;

import java.util.ArrayList;
import java.util.List;

public class DownloadsFragment extends Fragment {

    public final static String TAG = "DownloadsFragment";

    private MainActivity activity;
    private List<Document> mDownloads;
    private DownloadAdapter mAdapter;

    public DownloadsFragment() {
    }

    public DownloadsFragment(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);

        initVariable();
        initUI(view);

        loadDownloads();

        AdView adView= view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return view;
    }

    private void initVariable() {
        if (activity == null) {
            activity = (MainActivity) getActivity();
        }

        mDownloads = new ArrayList<>();
        mAdapter = new DownloadAdapter(activity, mDownloads);
    }

    private void initUI(View view) {
        ListView lstDownload = view.findViewById(R.id.lst_download);
        lstDownload.setAdapter(mAdapter);
        lstDownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Document document = mDownloads.get(position);
                if (StringHelper.isEmpty(document.file)) return;
                Uri uri = Uri.parse(document.file);
                if (uri != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(browserIntent);
                }
            }
        });
    }

    public void loadDownloads() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Document> documents = new ArrayList<>();
                DBManager.getInstance().getDocuments(documents);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloads.clear();
                        mDownloads.addAll(documents);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, 300);
    }


}
