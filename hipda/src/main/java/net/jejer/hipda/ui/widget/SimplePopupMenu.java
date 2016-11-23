package net.jejer.hipda.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2016-11-23.
 */

public class SimplePopupMenu {

    private Context mContext;
    LayoutInflater mInflater;
    private String[] mDatas;
    private AdapterView.OnItemClickListener mListener;

    public SimplePopupMenu(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setDatas(String[] datas) {
        mDatas = datas;
    }

    public void setListener(AdapterView.OnItemClickListener listener) {
        mListener = listener;
    }

    public void show() {
        View view = mInflater.inflate(R.layout.dialog_menu_actions, null);
        ListView listView = (ListView) view.findViewById(R.id.lv_menu_actions);

        listView.setAdapter(new MenuActionAdapter(mContext));

        final AlertDialog.Builder popDialog = new AlertDialog.Builder(mContext);
        popDialog.setView(view);
        final AlertDialog dialog = popDialog.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long row) {
                mListener.onItemClick(adapterView, view, position, row);
                dialog.dismiss();
            }
        });
    }

    private class MenuActionAdapter extends ArrayAdapter {
        public MenuActionAdapter(Context context) {
            super(context, 0, mDatas);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;
            if (convertView == null) {
                row = mInflater.inflate(R.layout.item_menu_action, parent, false);
            } else {
                row = convertView;
            }
            TextView text = (TextView) row.findViewById(R.id.action_text);
            text.setText(mDatas[position]);
            return row;
        }
    }

}
