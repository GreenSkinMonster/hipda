package net.jejer.hipda.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Utils;

/**
 * utils to deal with fragments
 * Created by GreenSkinMonster on 2015-09-01.
 */
public class FragmentUtils {

    public static FragmentArgs parse(Intent intent) {
        if (intent != null) {
            if (Constants.INTENT_NOTIFICATION.equals(intent.getAction())) {
                return parseNotification(
                        intent.getIntExtra(Constants.EXTRA_SMS_COUNT, -1),
                        intent.getIntExtra(Constants.EXTRA_THREAD_COUNT, -1),
                        intent.getStringExtra(Constants.EXTRA_UID),
                        intent.getStringExtra(Constants.EXTRA_USERNAME)
                );
            } else {
                Uri data = intent.getData();
                if (data != null) {
                    return FragmentUtils.parseUrl(data.toString());
                }
            }
        }
        return null;
    }

    private static FragmentArgs parseNotification(int smsCount, int threadCount, String uid, String username) {
        FragmentArgs args = null;
        if (smsCount == 1
                && threadCount == 0
                && HiUtils.isValidId(uid)
                && !TextUtils.isEmpty(username)) {
            args = new FragmentArgs();
            args.setType(FragmentArgs.TYPE_SMS_DETAIL);
            args.setUid(uid);
            args.setUsername(username);
        } else if (smsCount > 0) {
            args = new FragmentArgs();
            args.setType(FragmentArgs.TYPE_SMS);
        } else if (threadCount > 0) {
            args = new FragmentArgs();
            args.setType(FragmentArgs.TYPE_THREAD_NOTIFY);
        }
        return args;
    }

    public static FragmentArgs parseUrl(String url) {
        if (url.startsWith(HiUtils.BaseUrl + "forumdisplay.php")) {
            if (url.contains("fid")) {
                String fid = HttpUtils.getMiddleString(url, "fid=", "&");
                if (HiUtils.isValidId(fid) && HiUtils.isForumEnabled(Integer.parseInt(fid))) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_FORUM);
                    args.setFid(Integer.parseInt(fid));
                    return args;
                }
            }
        } else if (url.startsWith(HiUtils.BaseUrl + "viewthread.php")) {
            if (url.contains("tid")) {
                String tid = HttpUtils.getMiddleString(url, "tid=", "&");
                if (HiUtils.isValidId(tid)) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_THREAD);
                    args.setTid(tid);

                    String page = HttpUtils.getMiddleString(url, "page=", "&");
                    if (!TextUtils.isEmpty(page) && TextUtils.isDigitsOnly(page))
                        args.setPage(Integer.parseInt(page));

                    return args;
                }
            }
        } else if (url.startsWith(HiUtils.BaseUrl + "redirect.php")) {
            String gotoStr = HttpUtils.getMiddleString(url, "goto=", "&");
            if (!TextUtils.isEmpty(gotoStr)) {
                if ("lastpost".equals(gotoStr)) {
                    //goto last post
                    String tid = HttpUtils.getMiddleString(url, "tid=", "&");
                    if (HiUtils.isValidId(tid)) {
                        FragmentArgs args = new FragmentArgs();
                        args.setType(FragmentArgs.TYPE_THREAD);

                        args.setTid(tid);
                        args.setPage(ThreadDetailFragment.LAST_PAGE);
                        args.setFloor(ThreadDetailFragment.LAST_FLOOR);

                        return args;
                    }
                } else if ("findpost".equals(gotoStr)) {
                    //goto specific post by post id
                    String tid = HttpUtils.getMiddleString(url, "ptid=", "&");
                    String postId = HttpUtils.getMiddleString(url, "pid=", "&");

                    if (HiUtils.isValidId(tid) && HiUtils.isValidId(postId)) {
                        FragmentArgs args = new FragmentArgs();
                        args.setType(FragmentArgs.TYPE_THREAD);

                        args.setTid(tid);
                        args.setPostId(postId);

                        return args;
                    }
                }
            }
        } else if (url.startsWith(HiUtils.BaseUrl + "gotopost.php")) {
            //goto post by post id
            String postId = HttpUtils.getMiddleString(url, "pid=", "&");

            if (HiUtils.isValidId(postId)) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_THREAD);

                args.setPostId(postId);

                return args;
            }
        } else if (url.startsWith(HiUtils.BaseUrl + "space.php")) {
            //goto post by post id
            String uid = HttpUtils.getMiddleString(url, "uid=", "&");

            if (HiUtils.isValidId(uid)) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_SPACE);
                args.setUid(uid);
                return args;
            }
        }
        return null;
    }

    public static void showForum(FragmentManager fragmentManager, int fid) {
        //show forum always use Transaction.replace
        Bundle argments = new Bundle();
        if (HiUtils.isForumEnabled(fid))
            argments.putInt(ThreadListFragment.ARG_FID_KEY, fid);
        ThreadListFragment fragment = new ThreadListFragment();
        fragment.setArguments(argments);
        fragmentManager.beginTransaction()
                .replace(R.id.main_frame_container, fragment, fragment.getClass().getName())
                .commit();
    }

    public static void showThread(FragmentManager fragmentManager, boolean directOpen, String tid, String title, int page, int floor, String pid, int maxPage) {
        Bundle arguments = new Bundle();
        arguments.putString(ThreadDetailFragment.ARG_TID_KEY, tid);
        arguments.putString(ThreadDetailFragment.ARG_TITLE_KEY, title);
        arguments.putInt(ThreadDetailFragment.ARG_MAX_PAGE_KEY, maxPage);
        if (page != -1)
            arguments.putInt(ThreadDetailFragment.ARG_PAGE_KEY, page);
        if (floor != -1)
            arguments.putInt(ThreadDetailFragment.ARG_FLOOR_KEY, floor);
        if (HiUtils.isValidId(pid))
            arguments.putString(ThreadDetailFragment.ARG_PID_KEY, pid);
        ThreadDetailFragment fragment = new ThreadDetailFragment();
        fragment.setArguments(arguments);

        showFragment(fragmentManager, fragment, directOpen);
    }

    public static void showSpace(FragmentManager fragmentManager, boolean directOpen, String uid, String username) {
        Bundle arguments = new Bundle();
        arguments.putString(UserinfoFragment.ARG_UID, uid);
        arguments.putString(UserinfoFragment.ARG_USERNAME, username);
        UserinfoFragment fragment = new UserinfoFragment();
        fragment.setArguments(arguments);

        showFragment(fragmentManager, fragment, directOpen);
    }

    public static void showThreadNotify(FragmentManager fragmentManager, boolean directOpen) {
        Bundle notifyBundle = new Bundle();
        notifyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_THREAD_NOTIFY);
        SimpleListFragment fragment = new SimpleListFragment();
        fragment.setArguments(notifyBundle);
        showFragment(fragmentManager, fragment, directOpen);
    }

    public static void showSmsList(FragmentManager fragmentManager, boolean directOpen) {
        Bundle smsBundle = new Bundle();
        smsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_SMS);
        SimpleListFragment fragment = new SimpleListFragment();
        fragment.setArguments(smsBundle);
        showFragment(fragmentManager, fragment, directOpen);
    }

    public static void showSmsDetail(FragmentManager fragmentManager, boolean directOpen, String uid, String author) {
        Bundle smsBundle = new Bundle();
        smsBundle.putString(SmsFragment.ARG_AUTHOR, author);
        smsBundle.putString(SmsFragment.ARG_UID, uid);
        SmsFragment fragment = new SmsFragment();
        fragment.setArguments(smsBundle);
        showFragment(fragmentManager, fragment, directOpen);
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment, boolean skipEnterAnimation) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        int slideInAnim, slideOutAnim;
        if (HiSettingsHelper.getInstance().isNewAnimationType()) {
            slideInAnim = R.anim.slide_in_left;
            slideOutAnim = R.anim.slide_out_right;
        } else {
            if (Utils.getScreenWidth() <= 720) {
                slideInAnim = R.anim.slide_720_in_left;
                slideOutAnim = R.anim.slide_720_out_right;
            } else if (Utils.getScreenWidth() >= 1440) {
                slideInAnim = R.anim.slide_1440_in_left;
                slideOutAnim = R.anim.slide_1440_out_right;
            } else {
                slideInAnim = R.anim.slide_1080_in_left;
                slideOutAnim = R.anim.slide_1080_out_right;
            }
        }

        if (skipEnterAnimation)
            transaction.setCustomAnimations(0, 0, 0, slideOutAnim);
        else
            transaction.setCustomAnimations(slideInAnim, slideOutAnim, slideInAnim, slideOutAnim);

        transaction.add(R.id.main_frame_container, fragment, fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment) {
        showFragment(fragmentManager, fragment, false);
    }

    public static void show(FragmentManager fragmentManager, FragmentArgs args) {
        if (args.getType() == FragmentArgs.TYPE_THREAD)
            showThread(fragmentManager, args.isDirectOpen(), args.getTid(), "", args.getPage(), args.getFloor(), args.getPostId(), -1);
        else if (args.getType() == FragmentArgs.TYPE_SPACE)
            showSpace(fragmentManager, args.isDirectOpen(), args.getUid(), args.getUsername());
        else if (args.getType() == FragmentArgs.TYPE_SMS)
            showSmsList(fragmentManager, args.isDirectOpen());
        else if (args.getType() == FragmentArgs.TYPE_SMS_DETAIL)
            showSmsDetail(fragmentManager, args.isDirectOpen(), args.getUid(), args.getUsername());
        else if (args.getType() == FragmentArgs.TYPE_THREAD_NOTIFY)
            showThreadNotify(fragmentManager, args.isDirectOpen());
    }

}
