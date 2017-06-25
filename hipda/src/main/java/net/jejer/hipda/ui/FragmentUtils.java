package net.jejer.hipda.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import net.jejer.hipda.R;
import net.jejer.hipda.async.PostHelper;
import net.jejer.hipda.job.SimpleListJob;
import net.jejer.hipda.utils.Constants;
import net.jejer.hipda.utils.HiUtils;
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
            } else if (Constants.INTENT_SMS.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_SMS);
                return args;
            } else if (Constants.INTENT_SEARCH.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_SEARCH);
                return args;
            } else if (Constants.INTENT_FAVORITE.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_FAVORITE);
                return args;
            } else if (Constants.INTENT_NEW_THREAD.equals(intent.getAction())) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_NEW_THREAD);
                return args;
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
        if (url.contains(HiUtils.ForumUrlPattern + "forumdisplay.php")) {
            if (url.contains("fid")) {
                String fid = Utils.getMiddleString(url, "fid=", "&");
                if (HiUtils.isValidId(fid) && HiUtils.isForumValid(Integer.parseInt(fid))) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_FORUM);
                    args.setFid(Integer.parseInt(fid));
                    return args;
                }
            }
        } else if (url.contains(HiUtils.ForumUrlPattern + "viewthread.php")) {
            if (url.contains("tid")) {
                String tid = Utils.getMiddleString(url, "tid=", "&");
                if (HiUtils.isValidId(tid)) {
                    FragmentArgs args = new FragmentArgs();
                    args.setType(FragmentArgs.TYPE_THREAD);
                    args.setTid(tid);

                    String page = Utils.getMiddleString(url, "page=", "&");
                    if (!TextUtils.isEmpty(page) && TextUtils.isDigitsOnly(page))
                        args.setPage(Integer.parseInt(page));

                    return args;
                }
            }
        } else if (url.contains(HiUtils.ForumUrlPattern + "redirect.php")) {
            String gotoStr = Utils.getMiddleString(url, "goto=", "&");
            if (!TextUtils.isEmpty(gotoStr)) {
                if ("lastpost".equals(gotoStr)) {
                    //goto last post
                    String tid = Utils.getMiddleString(url, "tid=", "&");
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
                    String tid = Utils.getMiddleString(url, "ptid=", "&");
                    String postId = Utils.getMiddleString(url, "pid=", "&");

                    if (HiUtils.isValidId(tid) && HiUtils.isValidId(postId)) {
                        FragmentArgs args = new FragmentArgs();
                        args.setType(FragmentArgs.TYPE_THREAD);

                        args.setTid(tid);
                        args.setPostId(postId);

                        return args;
                    }
                }
            }
        } else if (url.contains(HiUtils.ForumUrlPattern + "gotopost.php")) {
            //goto post by post id
            String postId = Utils.getMiddleString(url, "pid=", "&");

            if (HiUtils.isValidId(postId)) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_THREAD);

                args.setPostId(postId);

                return args;
            }
        } else if (url.contains(HiUtils.ForumUrlPattern + "space.php")) {
            //goto post by post id
            String uid = Utils.getMiddleString(url, "uid=", "&");

            if (HiUtils.isValidId(uid)) {
                FragmentArgs args = new FragmentArgs();
                args.setType(FragmentArgs.TYPE_USER_INFO);
                args.setUid(uid);
                return args;
            }
        }
        return null;
    }

    public static void showForum(FragmentManager fragmentManager, int fid) {
        //show forum always use Transaction.replace
        Bundle argments = new Bundle();
        if (HiUtils.isForumValid(fid))
            argments.putInt(ThreadListFragment.ARG_FID_KEY, fid);
        ThreadListFragment fragment = new ThreadListFragment();
        fragment.setArguments(argments);
        fragmentManager.beginTransaction()
                .replace(R.id.main_frame_container, fragment, fragment.getClass().getName())
                .commitAllowingStateLoss();
    }

    public static void showThread(FragmentManager fragmentManager, boolean skipEnterAnim, String tid, String title, int page, int floor, String pid, int maxPage) {
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

        showFragment(fragmentManager, fragment, skipEnterAnim);
    }

    public static void showThreadActivity(Activity activity, boolean skipEnterAnim, String tid, String title, int page, int floor, String pid, int maxPage) {
        Intent intent = new Intent(activity, ThreadDetailActivity.class);
        intent.putExtra(ThreadDetailFragment.ARG_TID_KEY, tid);
        intent.putExtra(ThreadDetailFragment.ARG_TITLE_KEY, title);
        intent.putExtra(ThreadDetailFragment.ARG_MAX_PAGE_KEY, maxPage);
        if (page != -1)
            intent.putExtra(ThreadDetailFragment.ARG_PAGE_KEY, page);
        if (floor != -1)
            intent.putExtra(ThreadDetailFragment.ARG_FLOOR_KEY, floor);
        if (HiUtils.isValidId(pid))
            intent.putExtra(ThreadDetailFragment.ARG_PID_KEY, pid);

        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    private static Bundle getAnimBundle(Activity activity, boolean skipEnterAnim) {
        ActivityOptionsCompat options;
        if (skipEnterAnim) {
            options = ActivityOptionsCompat.makeBasic();
        } else {
            options = ActivityOptionsCompat.makeCustomAnimation(activity, R.anim.slide_in_right, 0);
        }
        return options.toBundle();
    }

    public static void showUserInfo(FragmentManager fragmentManager, boolean skipEnterAnim, String uid, String username) {
        Bundle arguments = new Bundle();
        arguments.putString(UserinfoFragment.ARG_UID, uid);
        arguments.putString(UserinfoFragment.ARG_USERNAME, username);
        UserinfoFragment fragment = new UserinfoFragment();
        fragment.setArguments(arguments);

        showFragment(fragmentManager, fragment, skipEnterAnim);
    }

    public static void showUserInfoActivity(Activity activity, boolean skipEnterAnim, String uid, String username) {
        Intent intent = new Intent(activity, UserInfoActivity.class);
        intent.putExtra(UserinfoFragment.ARG_UID, uid);
        intent.putExtra(UserinfoFragment.ARG_USERNAME, username);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static void showThreadNotify(FragmentManager fragmentManager, boolean skipEnterAnim) {
        Bundle notifyBundle = new Bundle();
        notifyBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListJob.TYPE_THREAD_NOTIFY);
        SimpleListFragment fragment = new SimpleListFragment();
        fragment.setArguments(notifyBundle);
        showFragment(fragmentManager, fragment, skipEnterAnim);
    }

    public static void showSmsList(FragmentManager fragmentManager, boolean skipEnterAnim) {
        Bundle smsBundle = new Bundle();
        smsBundle.putInt(SimpleListFragment.ARG_TYPE, SimpleListJob.TYPE_SMS);
        SimpleListFragment fragment = new SimpleListFragment();
        fragment.setArguments(smsBundle);
        showFragment(fragmentManager, fragment, skipEnterAnim);
    }

    public static void showSmsDetail(FragmentManager fragmentManager, boolean skipEnterAnim, String uid, String author) {
        Bundle smsBundle = new Bundle();
        smsBundle.putString(SmsFragment.ARG_AUTHOR, author);
        smsBundle.putString(SmsFragment.ARG_UID, uid);
        SmsFragment fragment = new SmsFragment();
        fragment.setArguments(smsBundle);
        showFragment(fragmentManager, fragment, skipEnterAnim);
    }

    public static void showSmsActivity(Activity activity, boolean skipEnterAnim, String uid, String author) {
        Intent intent = new Intent(activity, SmsActivity.class);
        intent.putExtra(SmsFragment.ARG_AUTHOR, author);
        intent.putExtra(SmsFragment.ARG_UID, uid);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static void showSimpleListActivity(Activity activity, boolean skipEnterAnim, int type) {
        Intent intent = new Intent(activity, SimpleListActivity.class);
        intent.putExtra(SimpleListFragment.ARG_TYPE, type);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, skipEnterAnim));
    }

    public static void showNewPostActivity(Activity activity, int fid, String parentSessionId) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra(PostFragment.ARG_MODE_KEY, PostHelper.MODE_NEW_THREAD);
        intent.putExtra(PostFragment.ARG_FID_KEY, fid);
        intent.putExtra(PostFragment.ARG_PARENT_ID, parentSessionId);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, true));
    }

    public static void showPostActivity(Activity activity, int mode, String parentSessionId,
                                        int fid, String tid, String postId, int floor,
                                        String author, String text, String quoteText) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra(PostFragment.ARG_MODE_KEY, mode);
        intent.putExtra(PostFragment.ARG_FID_KEY, fid);
        intent.putExtra(PostFragment.ARG_PARENT_ID, parentSessionId);
        intent.putExtra(PostFragment.ARG_TID_KEY, tid);
        intent.putExtra(PostFragment.ARG_PID_KEY, postId);
        intent.putExtra(PostFragment.ARG_FLOOR_KEY, floor);
        if (text != null)
            intent.putExtra(PostFragment.ARG_TEXT_KEY, text);
        if (author != null)
            intent.putExtra(PostFragment.ARG_FLOOR_AUTHOR_KEY, author);
        if (quoteText != null)
            intent.putExtra(PostFragment.ARG_QUOTE_TEXT_KEY, quoteText);
        ActivityCompat.startActivity(activity, intent, getAnimBundle(activity, true));
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment, boolean skipEnterAnim) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (skipEnterAnim) {
            transaction.setCustomAnimations(0, 0, 0, R.anim.slide_out_right);
        } else {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);
        }

        transaction.add(R.id.main_frame_container, fragment, fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commitAllowingStateLoss();
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment fragment) {
        showFragment(fragmentManager, fragment, false);
    }

    public static void show(FragmentActivity activity, FragmentArgs args) {
        if (args == null)
            return;
        if (args.getType() == FragmentArgs.TYPE_THREAD) {
            showThreadActivity(activity, args.isSkipEnterAnim(), args.getTid(), "", args.getPage(), args.getFloor(), args.getPostId(), -1);
        } else if (args.getType() == FragmentArgs.TYPE_USER_INFO) {
            showUserInfoActivity(activity, args.isSkipEnterAnim(), args.getUid(), args.getUsername());
        } else if (args.getType() == FragmentArgs.TYPE_SMS) {
            showSimpleListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_SMS);
        } else if (args.getType() == FragmentArgs.TYPE_SEARCH) {
            showSimpleListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_SEARCH);
        } else if (args.getType() == FragmentArgs.TYPE_FAVORITE) {
            showSimpleListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_FAVORITES);
        } else if (args.getType() == FragmentArgs.TYPE_SMS_DETAIL) {
            showSmsActivity(activity, args.isSkipEnterAnim(), args.getUid(), args.getUsername());
        } else if (args.getType() == FragmentArgs.TYPE_THREAD_NOTIFY) {
            showSimpleListActivity(activity, args.isSkipEnterAnim(), SimpleListJob.TYPE_THREAD_NOTIFY);
        } else if (args.getType() == FragmentArgs.TYPE_FORUM) {
            showForum(activity.getSupportFragmentManager(), args.getFid());
        } else if (args.getType() == FragmentArgs.TYPE_NEW_THREAD) {
            showNewPostActivity(activity, args.getFid(), args.getParentId());
        }
    }

}
