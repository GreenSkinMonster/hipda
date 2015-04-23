package net.jejer.hipda.ui;

import android.app.DownloadManager;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.textstyle.HiHtmlTagHandler;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextViewWithEmoticon extends TextView {
    private final static String LOG_TAG = "TextViewWithEmoticon";
    private static Context mCtx;
    private static FragmentManager mFragmentManager;

    private static int TRIM_LENGTH = 60;
    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    private boolean mTrim;

    public TextViewWithEmoticon(Context context) {
        super(context);
        mCtx = context;
        if (HiSettingsHelper.getInstance().isEinkModeUIEnabled())
            setLinkTextColor(mCtx.getResources().getColor(R.color.grey));
    }

    public TextViewWithEmoticon(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    public void setTrim(boolean trim) {
        mTrim = trim;
    }

    private boolean addImages(Context context, Spannable spannable) {
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
                Drawable icon = context.getResources().getDrawable(id);
                if (icon != null) {
                    icon.setBounds(0, 0, getLineHeight(), getLineHeight());
                    spannable.setSpan(new ImageSpan(icon, ImageSpan.ALIGN_BASELINE),
                            matcher.start(),
                            matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            }
        }

        return hasChanges;
    }

    private Spannable getTextWithImages(final Context context, CharSequence text) {
        if (mTrim)
            text = text.toString().replace("<br>", "").trim();
        SpannableStringBuilder b = (SpannableStringBuilder) Html.fromHtml(text.toString(), null, new HiHtmlTagHandler());
        if (mTrim && b.length() > TRIM_LENGTH) {
            b = new SpannableStringBuilder(b.subSequence(0, TRIM_LENGTH));
            b.append(" ....");
        }
        for (URLSpan s : b.getSpans(0, b.length(), URLSpan.class)) {
            String s_url = s.getURL();
            if (s_url.startsWith("http://www.hi-pda.com/forum/attachment.php")) {
                URLSpan newSpan = getDownloadUrlSpan(s_url);
                b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                b.removeSpan(s);
            } else if (s_url.startsWith("http://www.hi-pda.com/forum/viewthread.php")) {
                String tid = HttpUtils.getMiddleString(s_url, "tid=", "&");
                if (tid != null) {
                    URLSpan newSpan = getThreadUrlSpan(s_url);
                    b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                    b.removeSpan(s);
                }
            } else if (s_url.startsWith("http://www.hi-pda.com/forum/space.php")) {
                String uid = HttpUtils.getMiddleString(s_url, "uid=", "&");
                if (uid != null) {
                    URLSpan newSpan = getUserInfoUrlSpan(s_url);
                    b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                    b.removeSpan(s);
                }
            }
        }

        Spannable spannable = spannableFactory.newSpannable(b);
        addImages(context, spannable);
        return spannable;
    }

    private URLSpan getThreadUrlSpan(final String s_url) {
        return new URLSpan(s_url) {
            public void onClick(View view) {
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
    }

    private URLSpan getUserInfoUrlSpan(final String s_url) {
        return new URLSpan(s_url) {
            public void onClick(View view) {

                String uid = HttpUtils.getMiddleString(s_url, "uid=", "&");
                String username = "";

                Bundle arguments = new Bundle();
                arguments.putString(UserinfoFragment.ARG_UID, uid);
                arguments.putString(UserinfoFragment.ARG_USERNAME, username);
                UserinfoFragment fragment = new UserinfoFragment();
                fragment.setArguments(arguments);

                if (HiSettingsHelper.getInstance().getIsLandscape()) {
                    mFragmentManager.beginTransaction()
                            .replace(R.id.thread_detail_container_in_main, fragment, ThreadDetailFragment.class.getName())
                            .addToBackStack(ThreadDetailFragment.class.getName())
                            .commit();
                } else {
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
    }

    private URLSpan getDownloadUrlSpan(final String s_url) {
        return new URLSpan(s_url) {
            public void onClick(View view) {
                try {
                    DownloadManager dm = (DownloadManager) mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request downloadReq = new DownloadManager.Request(Uri.parse(getURL()));
                    downloadReq.addRequestHeader("User-agent", HiUtils.UserAgent);

                    String fileName = "";

                    //clean way to get fileName
                    SpannableStringBuilder b = new SpannableStringBuilder(((TextView) view).getText());
                    URLSpan[] urls = b.getSpans(0, b.length(), URLSpan.class);
                    if (urls.length > 0) {
                        fileName = b.toString().substring(b.getSpanStart(urls[0]), b.getSpanEnd(urls[0]));
                    }
                    if (TextUtils.isEmpty(fileName)) {
                        //failsafe dirty way,  to get rid of ( xxx K ) file size string
                        fileName = ((TextView) view).getText().toString();
                        if (fileName.contains(" ("))
                            fileName = fileName.substring(0, fileName.lastIndexOf(" (")).trim();
                    }
                    downloadReq.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    downloadReq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    dm.enqueue(downloadReq);
                } catch (SecurityException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    Toast.makeText(mCtx, "下载出现错误，请使用浏览器下载\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * http://stackoverflow.com/a/17246463/2299887
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = false;
        CharSequence text = getText();
        Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= getTotalPaddingLeft();
            y -= getTotalPaddingTop();

            x += getScrollX();
            y += getScrollY();

            Layout layout = getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = stext.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(this);
                }
                ret = true;
            }
        }
        return ret;
    }

}
