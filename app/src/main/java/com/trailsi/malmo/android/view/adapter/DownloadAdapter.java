package com.trailsi.malmo.android.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.trailsi.malmo.android.R;
import com.trailsi.malmo.android.model.Document;

import java.util.List;

public class DownloadAdapter extends BaseAdapter {

    Context mContext;
    private List<Document> objects;

    public DownloadAdapter(Context context, List<Document> objects) {
        super();
        this.mContext = context;
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public Document getItem(int paramInt) {
        return objects.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return 0L;
    }

    public View getView(final int position, View paramView, ViewGroup paramViewGroup) {

        View view = paramView;
        Holder holder;

        if (view == null) {
            view = LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.item_download, null);
            holder = new Holder();
            holder.txtName = view.findViewById(R.id.txt_name);
        } else {
            holder = (Holder) view.getTag();
        }

        final Document item = objects.get(position);

        holder.txtName.setText(item.name);

        view.setTag(holder);

        return view;
    }

    class Holder {
        TextView txtName;
    }
}