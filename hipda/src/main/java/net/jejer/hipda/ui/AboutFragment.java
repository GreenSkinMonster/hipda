package net.jejer.hipda.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

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

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_about, menu);
        super.onCreateOptionsMenu(menu, inflater);
        setActionBarTitle("关于");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bug_report:
                showReportDialog();
                return true;
            case R.id.action_jejer:
                setHasOptionsMenu(false);
                FragmentUtils.show(getFragmentManager(),
                        FragmentUtils.parseUrl(HiUtils.BaseUrl + "viewthread.php?tid=1408844")
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        setActionBarTitle("设置");
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

    public void showReportDialog() {
        AlertDialog.Builder builder = UIUtils.getMessageDialogBuilder(
                getActivity(),
                getActivity().getString(R.string.action_bug_report),
                getActivity().getString(R.string.report_reminder));
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setHasOptionsMenu(false);
                        FragmentUtils.show(getFragmentManager(),
                                FragmentUtils.parseUrl(HiUtils.BaseUrl + "viewthread.php?tid=1579403")
                        );
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
