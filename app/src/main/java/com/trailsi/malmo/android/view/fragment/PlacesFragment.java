package com.trailsi.malmo.android.view.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.trailsi.malmo.android.R;
import com.trailsi.malmo.android.common.Constant;
import com.trailsi.malmo.android.common.db.DBManager;
import com.trailsi.malmo.android.common.utils.SortList;
import com.trailsi.malmo.android.common.utils.StringHelper;
import com.trailsi.malmo.android.model.Audio;
import com.trailsi.malmo.android.model.AudioAds;
import com.trailsi.malmo.android.model.AudioType;
import com.trailsi.malmo.android.model.Location;
import com.trailsi.malmo.android.model.PurchaseResult;
import com.trailsi.malmo.android.view.activity.main.MainActivity;
import com.trailsi.malmo.android.view.adapter.PlacesAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlacesFragment extends Fragment {

    public final static String TAG = "PlacesFragment";

    private MainActivity activity;

    private ListView lstPlace;
    private List<Location> mLocations;
    private List<AudioAds> mAds;
    private PlacesAdapter mAdapter;

    private MediaPlayer mPlayer;

    public int mNewPointNumber;
    public boolean mNewPointPlayed;

    public PlacesFragment() {
    }

    public PlacesFragment(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places, container, false);

        initVariable();
        initUI(view);
        loadLocations();

        return view;
    }

    private void initVariable() {
        if (activity == null) {
            activity = (MainActivity) getActivity();
        }

        mLocations = new ArrayList<>();
        mAdapter = new PlacesAdapter(activity, this, mLocations);

        mNewPointNumber = 0;
        mNewPointPlayed = true; // SET this flag to true, so first time, it will play the audio

        mPlayer = new MediaPlayer();
        mAds = new ArrayList<>();
    }

    private void initUI(View view) {
        lstPlace = view.findViewById(R.id.lst_place);
        lstPlace.setAdapter(mAdapter);
        lstPlace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.updateView(view, position);

                Location location = mAdapter.getItem(position);
                if (location == null) {
                    return;
                }

                // If player is playing, in-app-purchase or downloading audios, skip it
                if (activity.isLoading) {
                    Log.e(TAG, "Playing, or loading");
                    return;
                }

                // If new POI is selected, but didn't play, skip it
                if (!mNewPointPlayed) {
                    Log.e(TAG, "New POI audio didn't played");
                    return;
                }
                mNewPointNumber = location.number;
                preparingAudio();
            }
        });
    }

    public void loadLocations() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Location> locations = new ArrayList<>();
                DBManager.getInstance().getLocations(locations);
                SortList.sortLocations(locations);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLocations.clear();

                        AudioType type = DBManager.getInstance().getCurrentAudioType();
                        for (Location location: locations) {
                            if (!location.visibility) {
                                continue;
                            }
                            if (type.hide_list) {
                                // If type is List customized, we can check if there is audio for this tour
                                Audio audio = DBManager.getInstance().getAudio(location.number);
                                if (audio != null) {
                                    mLocations.add(location);
                                }
                            } else {
                                mLocations.add(location);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, 300);
    }

    public void preparingAudio() {
        if (mNewPointNumber == 0) {
            return;
        }

        Audio audio = DBManager.getInstance().getAudio(mNewPointNumber);
        File file = activity.getAudioFile(audio);

        try {
            if (mPlayer.isPlaying()) {
                timer.cancel();
                mPlayer.stop();
            }

            mPlayer.reset();

            if (file == null) {
                mAdapter.updatePlayImage(false);
                return;
            }

            mAds.clear();
            if (audio.ads != null) {
                mAds.addAll(audio.ads);
            }
            mPlayer.setDataSource(file.getAbsolutePath());
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mAdapter.updatePlayViews(0, mPlayer.getDuration());

                    // Reset the last POI and flag
                    mNewPointPlayed = true;

                    playAudio();
                }
            });
            mPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // CountDownTimer
    CountDownTimer timer = new CountDownTimer(10000, 10) {

        public void onTick(long millisUntilFinished) {
            if (!mPlayer.isPlaying()) {
             //   activity.hideSnackBar();
                mAdapter.updatePlayImage(false);
                mAdapter.updatePlayViews(0, -1);
                timer.cancel();
            }
            int currentPosition = mPlayer.getCurrentPosition();
            mAdapter.updatePlayViews(currentPosition, -1);

            // Check AudioAds
            for (AudioAds ad : mAds) {
                if (ad.position == currentPosition / 1000) {
                 //   activity.showSnackBar(ad.url);
                    break;
                }
            }
        }

        public void onFinish() {
            timer.start();
        }

    };

    public void updateAudio() {
        if (mPlayer.isPlaying()) {
            pauseAudio();
        } else {
            playAudio();
        }
    }

    public void playAudio() {
        timer.start();
        mPlayer.start();
        mAdapter.updatePlayImage(true);
    }

    public void pauseAudio() {
        timer.cancel();
        mPlayer.pause();
        mAdapter.updatePlayImage(false);
    }

    public void updatePlayerProgress(int progress) {
        mPlayer.seekTo(progress);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        super.onDestroy();

        try {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
