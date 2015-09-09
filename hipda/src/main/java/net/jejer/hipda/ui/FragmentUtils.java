package net.jejer.hipda.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

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
                        intent.getIntExtra(Constants.EXTRA_THREAD_COUNT, -1));
            } else {
                Uri data = intent.getData();
                if (data != null) {
                    return FragmentUtils.parseUrl(data.toString());
                }
            }
        }
        return null;
    }

    private static FragmentArgs parseNotification(int smsCount, int threadCount) {
        FragmentArgs args = null;
        if (smsCount > 0) {
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

                    args.setTid(Integer.parseInt(tid));
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

                        args.setTid(Integer.parseInt(tid));
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

                        args.setTid(Integer.parseInt(tid));
                        args.setPostId(Integer.parseInt(postId));

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

                args.setPostId(Integer.parseInt(postId));

                return args;
            }
        } else if (url.startsWith(HiUtils.BaseUrl + "space.php")) {
            //goto post by post id
            String uid = HttpUtils.getMiddleString(url, "uid=", "&");

            if (HiUtils.isValidId(uid)) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_SPACE);
                args.setUid(Integer.parseInt(uid));
                return args;
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

    public static void showThread(FragmentManager fragmentManager, String tid, String title, int page, int floor, int pid, int maxPage) {
        Bundle arguments = new Bundle();
        arguments.putString(ThreadDetailFragment.ARG_TID_KEY, tid);
        arguments.putString(ThreadDetailFragment.ARG_TITLE_KEY, title);
        arguments.putInt(ThreadDetailFragment.ARG_MAX_PAGE_KEY, maxPage);
        if (page != -1)
            arguments.putInt(ThreadDetailFragment.ARG_PAGE_KEY, page);
        if (floor != -1)
            arguments.putInt(ThreadDetailFragment.ARG_FLOOR_KEY, floor);
        if (pid > -0)
            arguments.putString(ThreadDetailFragment.ARG_PID_KEY, pid + "");
        ThreadDetailFragment fragment = new ThreadDetailFragment();
        fragment.setArguments(arguments);
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                .addToBackStack(ThreadDetailFragment.class.getName())
                .commit();
    }

    public static void showSpace(FragmentManager fragmentManager, int uid) {
        Bundle arguments = new Bundle();
        arguments.putString(UserinfoFragment.ARG_UID, uid + "");
        arguments.putString(UserinfoFragment.ARG_USERNAME, "");
        UserinfoFragment fragment = new UserinfoFragment();
        fragment.setArguments(arguments);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                .addToBackStack(ThreadDetailFragment.class.getName())
                .commit();
    }

    public static void showThreadNotify(FragmentManager fragmentManager) {
        Bundle notifyBundle = new Bundle();
        notifyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_THREAD_NOTIFY);
        SimpleListFragment notifyFragment = new SimpleListFragment();
        notifyFragment.setArguments(notifyBundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(0, 0, 0, R.anim.slide_out_right);
        transaction.replace(R.id.main_frame_container, notifyFragment, SimpleListFragment.class.getName())
                .addToBackStack(SimpleListFragment.class.getName())
                .commit();
    }

    public static void showSms(FragmentManager fragmentManager) {
        Bundle smsBundle = new Bundle();
        smsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListLoader.TYPE_SMS);
        SimpleListFragment smsFragment = new SimpleListFragment();
        smsFragment.setArguments(smsBundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(0, 0, 0, R.anim.slide_out_right);
        transaction.replace(R.id.main_frame_container, smsFragment, SimpleListFragment.class.getName())
                .addToBackStack(SimpleListFragment.class.getName())
                .commit();
    }

    public static void show(FragmentManager fragmentManager, FragmentArgs args) {
        if (args.getType() == FragmentArgs.TYPE_THREAD)
            showThread(fragmentManager, args.getTid() + "", "", args.getPage(), args.getFloor(), args.getPostId(), -1);
        else if (args.getType() == FragmentArgs.TYPE_SPACE)
            showSpace(fragmentManager, args.getUid());
        else if (args.getType() == FragmentArgs.TYPE_SMS)
            showSms(fragmentManager);
        else if (args.getType() == FragmentArgs.TYPE_THREAD_NOTIFY)
            showThreadNotify(fragmentManager);
    }

}
