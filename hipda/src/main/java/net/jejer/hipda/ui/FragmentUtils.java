package net.jejer.hipda.ui;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

/**
 * utils to deal with fragments
 * Created by GreenSkinMonster on 2015-09-01.
 */
public class FragmentUtils {

    public static FragmentArgs parse(Intent intent) {
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null) {
                return FragmentUtils.parse(data.toString());
            }
        }
        return null;
    }

    public static FragmentArgs parse(String url) {
        if (url.startsWith("http://www.hi-pda.com/forum/forumdisplay.php")) {
            if (url.contains("fid")) {
                String fid = HttpUtils.getMiddleString(url, "fid=", "&");
                if (!TextUtils.isEmpty(fid) && TextUtils.isDigitsOnly(fid) && HiUtils.isForumEnabled(Integer.parseInt(fid))) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_FORUM);
                    args.setFid(Integer.parseInt(fid));
                    return args;
                }
            }
        } else if (url.startsWith("http://www.hi-pda.com/forum/viewthread.php")) {
            if (url.contains("tid")) {
                String tid = HttpUtils.getMiddleString(url, "tid=", "&");
                if (!TextUtils.isEmpty(tid) && TextUtils.isDigitsOnly(tid)) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_THREAD);

                    args.setTid(Integer.parseInt(tid));
                    String page = HttpUtils.getMiddleString(url, "page=", "&");
                    if (!TextUtils.isEmpty(page) && TextUtils.isDigitsOnly(page))
                        args.setPage(Integer.parseInt(page));

                    return args;
                }
            }
        }
        return null;
    }

    public static void showForum(FragmentManager fragmentManager, int fid) {
        Bundle argments = new Bundle();
        if (HiUtils.isForumEnabled(fid))
            argments.putInt(ThreadListFragment.ARG_FID_KEY, fid);
        ThreadListFragment threadListFragment = new ThreadListFragment();
        threadListFragment.setArguments(argments);
        fragmentManager.beginTransaction()
                .replace(R.id.main_frame_container, threadListFragment, ThreadListFragment.class.getName())
                .commit();
    }


    public static void showThread(FragmentManager fragmentManager, String tid, String title, int page, int floor, int maxPage) {
        Bundle arguments = new Bundle();
        arguments.putString(ThreadDetailFragment.ARG_TID_KEY, tid);
        arguments.putString(ThreadDetailFragment.ARG_TITLE_KEY, title);
        arguments.putInt(ThreadDetailFragment.ARG_MAX_PAGE_KEY, maxPage);
        if (page != -1)
            arguments.putInt(ThreadDetailFragment.ARG_PAGE_KEY, page);
        if (floor != -1)
            arguments.putInt(ThreadDetailFragment.ARG_FLOOR_KEY, floor);
        ThreadDetailFragment fragment = new ThreadDetailFragment();
        fragment.setArguments(arguments);
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                .addToBackStack(ThreadDetailFragment.class.getName())
                .commit();
    }

}
