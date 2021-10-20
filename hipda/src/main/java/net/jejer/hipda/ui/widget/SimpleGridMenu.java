package net.jejer.hipda.ui.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.ui.BaseFragment;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

/**
 * Created by GreenSkinMonster on 2017-06-24.
 */

public class SimpleGridMenu {

    private Context mContext;
    private BaseFragment mFragment;
    private LayoutInflater mInflater;
    private DetailBean mDetailBean;
    private AlertDialog mDialog;
    private DialogInterface.OnDismissListener mOnDismissListener;
    private View.OnClickListener mReportListener;


    private LinkedHashMap<String, MenuItem> mMenuItems = new LinkedHashMap<>();
    private List<String> mActionKeys = new ArrayList<>();

    public SimpleGridMenu(BaseFragment fragment) {
        mContext = fragment.getActivity();
        mFragment = fragment;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setDetailBean(DetailBean detailBean) {
        mDetailBean = detailBean;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void setReportListener(View.OnClickListener reportListener) {
        mReportListener = reportListener;
    }

    public void show() {
        View view = mInflater.inflate(R.layout.dialog_grid_menu, null);
        GridView gridView = view.findViewById(R.id.grid_view);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        ImageView avatarView = view.findViewById(R.id.iv_avatar);
        ImageView reportView = view.findViewById(R.id.iv_report);

        GlideHelper.loadAvatar(mFragment, avatarView, HiUtils.getAvatarUrlByUid(mDetailBean.getUid()));

        gridView.setAdapter(new MenuActionAdapter(mContext));
        tvTitle.setText(mDetailBean.getFloor() + "# " + mDetailBean.getAuthor());

        if (HiSettingsHelper.getInstance().getUid().equals(mDetailBean.getUid()))
            reportView.setVisibility(View.INVISIBLE);
        reportView.setOnClickListener(mReportListener);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);
        mDialog = builder.create();
        mDialog.setOnDismissListener(mOnDismissListener);
        mDialog.show();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long row) {
                String actionKey = (String) view.getTag();
                MenuItem menuItem = mMenuItems.get(actionKey);
                if (menuItem != null) {
                    menuItem.listener.onItemClick(adapterView, view, position, row);
                }
                dismiss();
            }
        });
    }

    public void dismiss() {
        if (mDialog != null)
            mDialog.dismiss();
    }

    private class MenuActionAdapter extends ArrayAdapter<String> {
        MenuActionAdapter(Context context) {
            super(context, 0, mActionKeys);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.item_grid_menu, parent, false);
            } else {
                view = convertView;
            }
            final String actionKey = mActionKeys.get(position);
            view.setTag(actionKey);

            final MenuItem menuItem = mMenuItems.get(actionKey);
            TextView textView = view.findViewById(R.id.action_text);
            textView.setText(mMenuItems.get(actionKey).actionName);

            ImageView imageView = view.findViewById(R.id.action_image);
            if (menuItem.icon != null) {
                imageView.setVisibility(View.VISIBLE);
                int pading = Utils.dpToPx(16);
                imageView.setPadding(pading, pading, pading, pading);
                imageView.setClickable(true);
                imageView.setImageDrawable(new IconicsDrawable(mContext, menuItem.icon)
                        .sizeDp(24).color(ContextCompat.getColor(mContext, R.color.background_grey)));
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (menuItem.iconListener != null)
                            menuItem.iconListener.onClick(v);
                        dismiss();
                    }
                });

            } else {
                imageView.setVisibility(View.GONE);
            }
            return view;
        }
    }

    public void add(String actionKey, String actionName, AdapterView.OnItemClickListener listener) {
        add(actionKey, actionName, listener, null, null);
    }

    public void add(String actionKey, String actionName, AdapterView.OnItemClickListener listener,
                    IIcon icon, View.OnClickListener iconListener) {
        MenuItem menuItem = new MenuItem();
        menuItem.actionKey = actionKey;
        menuItem.actionName = actionName;
        menuItem.listener = listener;
        menuItem.icon = icon;
        menuItem.iconListener = iconListener;
        mMenuItems.put(actionKey, menuItem);
        if (!mActionKeys.contains(actionKey))
            mActionKeys.add(actionKey);
    }

    private class MenuItem {
        String actionKey;
        String actionName;
        AdapterView.OnItemClickListener listener;
        IIcon icon;
        View.OnClickListener iconListener;
    }

}
