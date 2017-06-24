package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.jejer.hipda.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by GreenSkinMonster on 2016-11-23.
 */

public class SimplePopupMenu {

    private Context mContext;
    private LayoutInflater mInflater;

    private LinkedHashMap<String, String> mActions = new LinkedHashMap<>();
    private LinkedHashMap<String, AdapterView.OnItemClickListener> mListeners = new LinkedHashMap<>();
    private List<String> mActionKeys = new ArrayList<>();

    public SimplePopupMenu(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                String actionKey = (String) view.getTag();
                if (mListeners.containsKey(actionKey) && mListeners.get(actionKey) != null) {
                    mListeners.get(actionKey).onItemClick(adapterView, view, position, row);
                }
                dialog.dismiss();
            }
        });
    }

    private class MenuActionAdapter extends ArrayAdapter<String> {
        MenuActionAdapter(Context context) {
            super(context, 0, mActionKeys);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.item_menu_action, parent, false);
            } else {
                view = convertView;
            }
            String actionKey = mActionKeys.get(position);
            view.setTag(actionKey);
            TextView text = (TextView) view.findViewById(R.id.action_text);
            text.setText(mActions.get(actionKey));
            return view;
        }
    }

    public void add(String actionKey, String actionName, AdapterView.OnItemClickListener listener) {
        mActionKeys.add(actionKey);
        mActions.put(actionKey, actionName);
        mListeners.put(actionKey, listener);
    }

}
