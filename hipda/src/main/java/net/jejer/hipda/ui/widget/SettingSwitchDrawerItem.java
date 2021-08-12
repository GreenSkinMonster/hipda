package net.jejer.hipda.ui.widget;


import android.view.View;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.core.content.ContextCompat;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.BaseDescribeableDrawerItem;
import com.mikepenz.materialdrawer.model.BaseViewHolder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import net.jejer.hipda.R;

import java.util.List;

public class SettingSwitchDrawerItem extends BaseDescribeableDrawerItem<SettingSwitchDrawerItem, SettingSwitchDrawerItem.ViewHolder> {


    private boolean switchEnabled = true;

    private boolean checked = false;
    private View.OnClickListener onClickListener = null;

    public SettingSwitchDrawerItem withChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    public SettingSwitchDrawerItem withOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    @Override
    public int getType() {
        return R.id.material_drawer_item_primary_switch;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.material_drawer_item_setting;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new SettingSwitchDrawerItem.ViewHolder(v);
    }

    @Override
    public void bindView(final SettingSwitchDrawerItem.ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        //bind the basic view parts
        bindViewHelper(viewHolder);

        //handle the switch
        viewHolder.imageView.setOnClickListener(onClickListener);
        if (checked) {
            viewHolder.imageView.setImageDrawable(
                    ContextCompat.getDrawable(viewHolder.imageView.getContext(), R.drawable.outline_light_mode_white_24)
            );
        } else {
            viewHolder.imageView.setImageDrawable(
                    ContextCompat.getDrawable(viewHolder.imageView.getContext(), R.drawable.outline_dark_mode_24)
            );
        }

        //add a onDrawerItemClickListener here to be able to check / uncheck if the drawerItem can't be selected
        withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                if (!isSelectable()) {
                    checked = !checked;
                    if (checked) {
                        viewHolder.imageView.setImageDrawable(
                                ContextCompat.getDrawable(viewHolder.imageView.getContext(), R.drawable.outline_light_mode_white_24)
                        );
                    } else {
                        viewHolder.imageView.setImageDrawable(
                                ContextCompat.getDrawable(viewHolder.imageView.getContext(), R.drawable.outline_dark_mode_24)
                        );
                    }
                }
                return false;
            }
        });

        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, viewHolder.itemView);
    }

    public static class ViewHolder extends BaseViewHolder {
        private ImageView imageView;

        private ViewHolder(View view) {
            super(view);
            this.imageView = view.findViewById(R.id.material_drawer_switch);
        }
    }
}
