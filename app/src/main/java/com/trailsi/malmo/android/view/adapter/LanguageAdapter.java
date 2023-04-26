package com.trailsi.malmo.android.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trailsi.malmo.android.R;
import com.trailsi.malmo.android.common.Constant;
import com.trailsi.malmo.android.common.cache.CachedData;
import com.trailsi.malmo.android.common.db.DBManager;
import com.trailsi.malmo.android.common.utils.StringHelper;
import com.trailsi.malmo.android.model.Language;

import java.util.ArrayList;
import java.util.List;

public class LanguageAdapter extends BaseAdapter {

    Context mContext;
    private List<Language> languages;

    public LanguageAdapter(Context context, List<Language> languages) {
        super();
        this.mContext = context;
        this.languages = languages;
    }

    public int getCount() {
        return languages.size();
    }

    public Language getItem(int paramInt) {
        return languages.get(paramInt);
    }

    public long getItemId(int paramInt) {
        return 0L;
    }

    public View getView(final int position, View paramView, ViewGroup paramViewGroup) {

        View view = paramView;
        Holder holder;

        if (view == null) {
            view = LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.item_language, null);
            holder = new Holder();
            holder.imgCheck = view.findViewById(R.id.img_check);
            holder.txtName = view.findViewById(R.id.txt_name);
        } else {
            holder = (Holder) view.getTag();
        }

        final Language item = getItem(position);
        String selectedLanguage = CachedData.getString(CachedData.kLanguage, "");

        if (StringHelper.isEmpty(selectedLanguage)) {
            holder.imgCheck.setVisibility(View.INVISIBLE);
        } else {
            holder.imgCheck.setVisibility(selectedLanguage.equals(item.isoCode) ? View.VISIBLE : View.INVISIBLE);
        }

        holder.txtName.setText(item.language);

        view.setTag(holder);

        return view;
    }

    class Holder {
        ImageView imgCheck;
        TextView txtName;
    }
}