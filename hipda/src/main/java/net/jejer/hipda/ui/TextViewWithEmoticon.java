package net.jejer.hipda.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.ui.textstyle.HiHtmlTagHandler;
import net.jejer.hipda.utils.ColorUtils;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.HttpUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

public class TextViewWithEmoticon extends TextView {
    private Context mCtx;
    private FragmentManager mFragmentManager;

    private static int TRIM_LENGTH = 80;

    private boolean mTrim;

    public TextViewWithEmoticon(Context context) {
        super(context);
        mCtx = context;
        init();
    }

    public TextViewWithEmoticon(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        init();
    }

    private void init() {
        setTextColor(ColorUtils.getDefaultTextColor(mCtx));
        setLinkTextColor(ColorUtils.getColorAccent(mCtx));

        float lineSpacingExtra = 2;
        float lineSpacingMultiplier = 1.1f;
        if (HiSettingsHelper.getInstance().getPostLineSpacing() == 1) {
            lineSpacingExtra = 4;
            lineSpacingMultiplier = 1.2f;
        } else if (HiSettingsHelper.getInstance().getPostLineSpacing() == 2) {
            lineSpacingExtra = 6;
            lineSpacingMultiplier = 1.3f;
        } else if (HiSettingsHelper.getInstance().getPostLineSpacing() == 3) {
            lineSpacingExtra = 8;
            lineSpacingMultiplier = 1.4f;
        }
        setLineSpacing(lineSpacingExtra, lineSpacingMultiplier);
    }

    public void setFragmentManager(FragmentManager fm) {
        mFragmentManager = fm;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        Spannable s = getTextWithImages(text);
        super.setText(s, BufferType.SPANNABLE);
    }

    public void setTrim(boolean trim) {
        mTrim = trim;
    }

    private Html.ImageGetter imageGetter = new Html.ImageGetter() {
        public Drawable getDrawable(String src) {
            Drawable icon = null;
            src = Utils.nullToText(src);
            int idx = src.indexOf(HiUtils.SMILE_PATH);
            if (idx != -1 && src.indexOf(".", idx) != -1) {
                src = src.substring(src.indexOf(HiUtils.SMILE_PATH) + HiUtils.SMILE_PATH.length(), src.lastIndexOf(".")).replace("/", "_");
                int id = mCtx.getResources().getIdentifier(src, "drawable", mCtx.getPackageName());
                if (id != 0) {
                    icon = mCtx.getResources().getDrawable(id);
                    if (icon != null)
                        icon.setBounds(0, 0, getLineHeight(), getLineHeight());
                }
            }
            return icon;
        }
    };

    private Spannable getTextWithImages(CharSequence text) {
        String t = text.toString().trim();
        //remove leading spaces
        while (t.startsWith("&nbsp;") || t.startsWith("<br>")) {
            if (t.startsWith("&nbsp;"))
                t = t.substring("&nbsp;".length()).trim();
            else
                t = t.substring("<br>".length()).trim();
        }
        if (mTrim)
            t = t.replace("<br>", "").trim();
        SpannableStringBuilder b = (SpannableStringBuilder) Html.fromHtml(t, imageGetter, new HiHtmlTagHandler());
        if (mTrim && b.length() > TRIM_LENGTH) {
            b = new SpannableStringBuilder(b.subSequence(0, TRIM_LENGTH));
            b.append(" ....");
        }
        for (URLSpan s : b.getSpans(0, b.length(), URLSpan.class)) {
            String s_url = s.getURL();
            if (s_url.startsWith(HiUtils.BaseUrl + "attachment.php")) {
                URLSpan newSpan = getDownloadUrlSpan(s_url);
                b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                b.removeSpan(s);
            } else {
                FragmentArgs args = FragmentUtils.parseUrl(s_url);
                if (args != null) {
                    URLSpan newSpan = getFragmentArgsUrlSpan(s_url);
                    b.setSpan(newSpan, b.getSpanStart(s), b.getSpanEnd(s), b.getSpanFlags(s));
                    b.removeSpan(s);
                }
            }
        }
        return b;
    }

    private URLSpan getFragmentArgsUrlSpan(final String s_url) {
        return new URLSpan(s_url) {
            public void onClick(View view) {
                FragmentArgs args = FragmentUtils.parseUrl(s_url);
                if (args != null) {
                    //this line is needed, or onCreateOptionsMenu and onPrepareOptionsMenu will be called multiple times
                    mFragmentManager.findFragmentById(R.id.main_frame_container).setHasOptionsMenu(false);
                    FragmentUtils.show(mFragmentManager, args);
                }
            }
        };
    }

    private URLSpan getDownloadUrlSpan(final String s_url) {
        return new URLSpan(s_url) {
            public void onClick(View view) {
                try {

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

                    HttpUtils.download(mCtx, getURL(), fileName);
                } catch (SecurityException e) {
                    Logger.e(e);
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
