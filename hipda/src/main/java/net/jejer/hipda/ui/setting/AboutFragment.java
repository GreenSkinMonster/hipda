package net.jejer.hipda.ui.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import net.jejer.hipda.R;
import net.jejer.hipda.ui.BaseFragment;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.Utils;

/**
 * show version and info
 * Created by GreenSkinMonster on 2015-05-23.
 */
public class AboutFragment extends BaseFragment {

    public static final String TAG_KEY = "ABOUT_KEY";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        final TextView tabContent = (TextView) view.findViewById(R.id.tab_content);
        final ScrollView scrollView = (ScrollView) view.findViewById(R.id.scroll_view);

        TextView tvAppVersion = (TextView) view.findViewById(R.id.app_version);
        tvAppVersion.setText(
                getResources().getString(R.string.app_name) + " " + HiApplication.getAppVersion()
                        + "\n" + getResources().getString(R.string.author));

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        TabLayout.Tab notesTab = tabLayout.newTab().setText("更新记录");
        TabLayout.Tab donarTab = tabLayout.newTab().setText("捐助名单");
        TabLayout.Tab linksTab = tabLayout.newTab().setText("感谢");

        tabLayout.addTab(notesTab);
        tabLayout.addTab(donarTab);
        tabLayout.addTab(linksTab);

        tabContent.setText(getContent(0));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                scrollView.scrollTo(0, 0);
                tabContent.setText(getContent(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                scrollView.scrollTo(0, 0);
            }
        });

        setActionBarTitle("关于");
        return view;
    }

    private String getContent(int position) {
        String file = "release-notes.txt";
        if (position == 1) {
            file = "donors.txt";
        } else if (position == 2) {
            file = "license.txt";
        }

        try {
            return Utils.readFromAssets(getActivity(), file);
        } catch (Exception e) {
            return e.getMessage();
        }

    }

}
