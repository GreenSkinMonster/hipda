package net.jejer.hipda.utils;

import android.graphics.Color;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import net.jejer.hipda.R;

/**
 * Created by GreenSkinMonster on 2016-07-27.
 */
public class DrawerHelper {

    public enum DrawerItem {
        SEARCH(Constants.DRAWER_SEARCH, R.string.title_drawer_search, GoogleMaterial.Icon.gmd_search),
        MY_POST(Constants.DRAWER_MYPOST, R.string.title_drawer_mypost, GoogleMaterial.Icon.gmd_assignment_ind),
        MY_REPLY(Constants.DRAWER_MYREPLY, R.string.title_drawer_myreply, GoogleMaterial.Icon.gmd_assignment),
        MY_FAVORITES(Constants.DRAWER_FAVORITES, R.string.title_drawer_favorites, GoogleMaterial.Icon.gmd_favorite),
        HISTORIES(Constants.DRAWER_HISTORIES, R.string.title_drawer_histories, GoogleMaterial.Icon.gmd_history),
        SMS(Constants.DRAWER_SMS, R.string.title_drawer_sms, GoogleMaterial.Icon.gmd_email, true),
        THREAD_NOTIFY(Constants.DRAWER_THREADNOTIFY, R.string.title_drawer_notify, GoogleMaterial.Icon.gmd_notifications, true),
        SETTINGS(Constants.DRAWER_SETTINGS, R.string.title_drawer_setting, GoogleMaterial.Icon.gmd_settings);

        public final int id;
        public final int name;
        public final IIcon icon;
        public final boolean withBadge;

        DrawerItem(int id, int name, IIcon icon, boolean withBadge) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.withBadge = withBadge;
        }

        DrawerItem(int id, int name, IIcon icon) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            withBadge = false;
        }
    }

    public static IDrawerItem getPrimaryMenuItem(DrawerItem drawerItem) {
        PrimaryDrawerItem primaryDrawerItem = new PrimaryDrawerItem()
                .withName(drawerItem.name)
                .withIdentifier(drawerItem.id)
                .withIcon(drawerItem.icon);

        if (drawerItem.withBadge) {
            primaryDrawerItem
                    .withBadgeStyle(new BadgeStyle()
                            .withTextColor(Color.WHITE)
                            .withColorRes(R.color.grey));
        }
        return primaryDrawerItem;
    }

    public static IDrawerItem getSecondaryMenuItem(DrawerItem drawerItem) {
        PrimaryDrawerItem secondaryDrawerItem = new SecondaryDrawerItem()
                .withName(drawerItem.name)
                .withIdentifier(drawerItem.id)
                .withIcon(drawerItem.icon);

        if (drawerItem.withBadge) {
            secondaryDrawerItem
                    .withBadgeStyle(new BadgeStyle()
                            .withTextColor(Color.WHITE)
                            .withColorRes(R.color.grey));
        }
        return secondaryDrawerItem;
    }

}
