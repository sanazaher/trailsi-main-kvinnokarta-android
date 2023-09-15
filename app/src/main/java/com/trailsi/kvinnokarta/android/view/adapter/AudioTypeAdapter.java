package com.trailsi.kvinnokarta.android.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.cache.CachedData;
import com.trailsi.kvinnokarta.android.common.db.DBManager;
import com.trailsi.kvinnokarta.android.common.utils.StringHelper;
import com.trailsi.kvinnokarta.android.model.AudioType;
import com.trailsi.kvinnokarta.android.model.PurchaseResult;

import java.util.List;

public class AudioTypeAdapter extends RecyclerView.Adapter<AudioTypeAdapter.CustomViewHolder> {

    Context mContext;
    private List<AudioType> audioTypes;
    private boolean showLockIcon;

    public AudioTypeAdapter(Context context, List<AudioType> types, boolean showLockIcon) {
        this.mContext = context;
        this.audioTypes = types;
        this.showLockIcon = showLockIcon;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_audio_type, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int i) {
        AudioType audioType = getItem(i);
        String selectedType = CachedData.getString(CachedData.kAudioType, "");

        boolean selected;
        if (StringHelper.isEmpty(selectedType)) {
            selected = false;
        } else {
            selected = selectedType.equals(audioType.name);
        }

        holder.layoutBackground.setSelected(selected);
        holder.txtType.setText(audioType.name);
        holder.txtType.setTextColor(mContext.getResources().getColor(selected ? R.color.colorPrimary : R.color.color_text));
        switch (i) {
            case 0:
                holder.imgType.setImageResource(selected ? R.drawable.img_walk_white : R.drawable.img_walk);
                break;
            case 1:
                holder.imgType.setImageResource(selected ? R.drawable.img_bike_white : R.drawable.img_bike);
                break;
            default:
                holder.imgType.setImageResource(selected ? R.drawable.img_group_white : R.drawable.img_group);
        }
   /*     if (this.showLockIcon) {
            holder.imgLock.setVisibility(View.VISIBLE);
            PurchaseResult result = DBManager.getInstance().getPurchaseResultByTour(audioType.name);
            if (result != null && result.purchased && !StringHelper.isEmpty(result.purchase_token)) {
                holder.imgLock.setImageResource(R.drawable.ic_unlocked);
            } else {
                holder.imgLock.setImageResource(R.drawable.ic_locked);
            }
        } else {
            holder.imgLock.setVisibility(View.GONE);
        }

    */
    }

    @Override
    public int getItemCount() {
        return audioTypes.size();
    }

    public AudioType getItem(int paramInt) {
        return audioTypes.get(paramInt);
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout layoutBackground;
        ImageView imgType;
        TextView txtType;
        ImageView imgLock;

        CustomViewHolder(View view) {
            super(view);
            this.layoutBackground = view.findViewById(R.id.layout_background);
            this.imgType = view.findViewById(R.id.img_type);
            this.txtType = view.findViewById(R.id.txt_type);
            this.imgLock = view.findViewById(R.id.img_lock);
        }
    }
}
