package net.jejer.hipda.ui.adapter;

import android.app.Activity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.jejer.hipda.R;

import java.util.Map;

/**
 * Created by GreenSkinMonster on 2016-04-12.
 */
public class ThreadTypeAdapter extends BaseAdapter {

    private Activity mContext;
    private Map<String, String> mTypes;


    public ThreadTypeAdapter(Activity activity, Map<String, String> types) {
        mContext = activity;
        mTypes = types;
    }

    @Override
    public int getCount() {
        return mTypes.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String key = mTypes.keySet().toArray()[position].toString();

        View row;
        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            row = inflater.inflate(R.layout.item_thread_type, parent, false);
        } else {
            row = convertView;
        }
        TextView textView = (TextView) row.findViewById(R.id.thread_type_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        textView.setText(mTypes.get(key));
        return row;
    }
}
