package net.jejer.hipda.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.jejer.hipda.R;

/**
 * show version and info
 * Created by GreenSkinMonster on 2015-05-23.
 */
public class AboutFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        ViewGroup aboutLayout = (ViewGroup) view.findViewById(R.id.about_layout);

        TextView tvAppVersion = (TextView) view.findViewById(R.id.app_version);
        tvAppVersion.setText(getResources().getString(R.string.app_name) + " " + HiApplication.getAppVersion());

        String[] credits = getResources().getStringArray(R.array.credits);

        for (String credit : credits) {
            ViewGroup itemAboutLayout = (ViewGroup) LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_about, container, false);

            TextViewWithEmoticon tvTitle = (TextViewWithEmoticon) itemAboutLayout.findViewById(R.id.about_title);

            tvTitle.setFragment(this);
            tvTitle.setText(credit);

            aboutLayout.addView(itemAboutLayout);
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        setActionBarTitle("关于");
    }

    @Override
    public void onStop() {
        super.onStop();
        setActionBarTitle("设置");
    }
}
