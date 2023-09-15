package com.trailsi.kvinnokarta.android.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.db.DBManager;
import com.trailsi.kvinnokarta.android.common.utils.TimeHelper;
import com.trailsi.kvinnokarta.android.model.Location;
import com.trailsi.kvinnokarta.android.view.activity.main.MainActivity;
import com.trailsi.kvinnokarta.android.view.fragment.PlacesFragment;

import java.io.File;
import java.util.List;

public class PlacesAdapter extends BaseAdapter {

    MainActivity activity;
    PlacesFragment mFragment;
    private List<Location> objects;
    private int mIndex;

    public PlacesAdapter(MainActivity context, PlacesFragment fragment, List<Location> objects) {
        super();
        this.activity = context;
        this.mFragment = fragment;
        this.objects = objects;
        this.mIndex = -1;
    }

    public int getCount() {
        return objects.size();
    }

    public Location getItem(int paramInt) {
        return objects.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return 0L;
    }

    public View getView(final int position, View paramView, ViewGroup paramViewGroup) {

        View view = paramView;
        Holder holder;

        if (view == null) {
            view = LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.item_place, null);
            holder = new Holder();
            holder.layoutContent = view.findViewById(R.id.layout_content);
            holder.imgPhoto = view.findViewById(R.id.img_photo);
            holder.txtName = view.findViewById(R.id.txt_name);
            holder.layoutPlayer = view.findViewById(R.id.layout_player);
            holder.imgPlay = view.findViewById(R.id.img_play);
            holder.seekBar = view.findViewById(R.id.seekbar);
        } else {
            holder = (Holder) view.getTag();
        }

        Location item = objects.get(position);

        if (position != mIndex) {
            holder.layoutContent.setBackgroundResource(R.color.color_white);
        } else {
            holder.layoutContent.setBackgroundResource(R.color.color_selected);
        }
        File image = DBManager.getInstance().getRealFile(activity, item.image);
        if (image != null && image.exists()) {
            Glide.with(activity)
                    .load(image)
                    .into(holder.imgPhoto);
        }
        holder.txtName.setText(item.name);

        if (position != mIndex) {
            holder.layoutPlayer.setVisibility(View.GONE);
        } else {
            holder.layoutPlayer.setVisibility(View.VISIBLE);
        }

        view.setTag(holder);

        return view;
    }

    static class Holder {
        LinearLayout layoutContent;
        ImageView imgPhoto;
        TextView txtName;
        LinearLayout layoutPlayer;
        ImageView imgPlay;
        SeekBar seekBar;
    }

    private SeekBar seekBar;
    private ImageView imgPlay;
    private TextView txtStartTime, txtEndTime;

    public void updateView(View view, int index) {
        if (mIndex == index) {
            return;
        }
        mIndex = index;
        notifyDataSetChanged();

        seekBar = view.findViewById(R.id.seekbar);
        imgPlay = view.findViewById(R.id.img_play);
        txtStartTime = view.findViewById(R.id.txt_start_time);
        txtEndTime = view.findViewById(R.id.txt_end_time);

        seekBar.setMax(0);
        txtStartTime.setText(TimeHelper.getTimeFromMilliseconds(0));
        txtEndTime.setText(TimeHelper.getTimeFromMilliseconds(0));

        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragment.updateAudio();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    try {
                        mFragment.updatePlayerProgress(progress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void updatePlayViews(int progress, int max) {
        if (seekBar != null) {
            if (max >= 0) seekBar.setMax(max);
            if (progress >= 0) seekBar.setProgress(progress);
        }

        if (txtStartTime != null) {
            if (progress > 0) txtStartTime.setText(TimeHelper.getTimeFromMilliseconds(progress));
            else txtStartTime.setText(TimeHelper.getTimeFromMilliseconds(0));
        }
        if (txtEndTime != null) {
            if (max >= 0) txtEndTime.setText(TimeHelper.getTimeFromMilliseconds(max));
        }
    }

    public void updatePlayImage(boolean isPlay) {
        if (imgPlay == null) {
            return;
        }
        if (isPlay) {
            imgPlay.setImageResource(R.drawable.ic_pause);
        } else {
            imgPlay.setImageResource(R.drawable.ic_play);
        }
    }
}