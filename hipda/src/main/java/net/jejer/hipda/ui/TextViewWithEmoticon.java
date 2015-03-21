package net.jejer.hipda.ui;

import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextViewWithEmoticon extends TextView {
    private final static String LOG_TAG = "TextViewWithEmoticon";
    private static Context mCtx;
    private static FragmentManager mFragmentManager;

    public TextViewWithEmoticon(Context context) {
        super(context);
        mCtx = context;
        if (HiSettingsHelper.getInstance().isEinkModeUIEnabled())
            setLinkTextColor(mCtx.getResources().getColor(R.color.grey));
    }

    public void setFragmentManager(FragmentManager fm) {
        mFragmentManager = fm;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        Spannable s = getTextWithImages(mCtx, text);
        super.setText(s, BufferType.SPANNABLE);
    }

    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    private static boolean addImages(Context context, Spannable spannable) {
        Pattern refImg = Pattern.compile("\\Q[emoticon images/smilies/\\E([a-zA-Z0-9_\\/]+)\\Q.gif]\\E");
        boolean hasChanges = false;

        Matcher matcher = refImg.matcher(spannable);
        while (matcher.find()) {
            boolean set = true;
            for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end()
                        ) {
                    spannable.removeSpan(span);
                } else {
                    set = false;
                    break;
                }
            }
            String resname = spannable.subSequence(matcher.start(1), matcher.end(1)).toString().trim();
            resname = resname.replaceAll("/", "_");
            int id = context.getResources().getIdentifier(resname, "drawable", context.getPackageName());
            if (set && id != 0) {
                hasChanges = true;
                spannable.setSpan(new ImageSpan(context, id),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        return hasChanges;
    }

    private static SpannableStringBuilder addAppMark(Context context, SpannableStringBuilder spannable) {
        String text = spannable.toString();
        int idxStart = text.indexOf("[appmark ");
        if (idxStart >= 0) {
            int idxEnd = text.indexOf("]", idxStart);
            if (idxEnd > 0) {
                spannable.setSpan(new RelativeSizeSpan(0.75f), idxStart, idxEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable = spannable.delete(idxEnd, idxEnd + 1).delete(idxStart, idxStart + 9);
            }
        }
        return spannable;
    }

    private static Spannable getTextWithImages(final Context context, CharSequence text) {
        SpannableStringBuilder b = (SpannableStringBuilder) Html.fromHtml(text.toString());
        for (URLSpan s : b.getSpans(0, b.length(), URLSpan.class)) {
            String s_url = s.getURL();
            if (s_url.startsWith("http://www.hi-pda.com/forum/attachment.php")) {
                URLSpan newSpan = new URLSpan(s_url) {
                    public void onClick(View view) {
                        DownloadManager dm = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request downloadReq = new DownloadManager.Request(Uri.parse(getURL()));
                        downloadReq.addRequestHeader("Cookie", "cdb_auth=" + HiSettingsHelper.getInstance().getCookieAuth());
                        downloadReq.addRequestHeader("User-agent", HiUtils.UserAgent);

                        // FUCK Android, we cannot use pub_download directory and keep original filename!
                        downloadReq.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ((TextView) view).getText().toString());
                        downloadReq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        dm.enqueue(downloadReq);
                    }
                };
                b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                b.removeSpan(s);
                continue;
            }
            if (s_url.startsWith("http://www.hi-pda.com/forum/viewthread.php")) {
                String tid = HttpUtils.getMiddleString(s_url, "tid=", "&");
                if (tid != null) {
                    URLSpan newSpan = new URLSpan(s_url) {
                        public void onClick(View view) {
                            Log.v(LOG_TAG, "ID=" + view.getRootView().getId());
                            Log.v(LOG_TAG, "URLSpan.onClick TID=" + HttpUtils.getMiddleString(getURL(), "tid=", "&"));

                            Bundle arguments = new Bundle();
                            arguments.putString(ThreadDetailFragment.ARG_TID_KEY, HttpUtils.getMiddleString(getURL(), "tid=", "&"));
                            arguments.putString(ThreadDetailFragment.ARG_TITLE_KEY, "");
                            ThreadDetailFragment fragment = new ThreadDetailFragment();
                            fragment.setArguments(arguments);

                            if (HiSettingsHelper.getInstance().getIsLandscape()) {
                                mFragmentManager.findFragmentById(R.id.thread_detail_container_in_main).setHasOptionsMenu(false);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.thread_detail_container_in_main, fragment, ThreadDetailFragment.class.getName())
                                        .addToBackStack(ThreadDetailFragment.class.getName())
                                        .commit();
                            } else {
                                mFragmentManager.findFragmentById(R.id.main_frame_container).setHasOptionsMenu(false);
                                if (HiSettingsHelper.getInstance().isEinkModeUIEnabled()) {
                                    mFragmentManager.beginTransaction()
                                            .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                                            .addToBackStack(ThreadDetailFragment.class.getName())
                                            .commit();
                                } else {
                                    mFragmentManager.beginTransaction()
                                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                                            .add(R.id.main_frame_container, fragment, ThreadDetailFragment.class.getName())
                                            .addToBackStack(ThreadDetailFragment.class.getName())
                                            .commit();
                                }
                            }
                        }
                    };
                    b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                    b.removeSpan(s);
                }
                continue;
            }
        }

        b = addAppMark(context, b);

        Spannable spannable = spannableFactory.newSpannable(b);
        addImages(context, spannable);
        return spannable;
    }

}
