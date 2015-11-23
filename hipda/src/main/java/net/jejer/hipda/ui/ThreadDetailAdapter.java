package net.jejer.hipda.ui;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentAbs;
import net.jejer.hipda.bean.ContentAttach;
import net.jejer.hipda.bean.ContentGoToFloor;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.ContentQuote;
import net.jejer.hipda.bean.ContentText;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageEvent;
import net.jejer.hipda.glide.GlideImageJob;
import net.jejer.hipda.glide.GlideImageManager;
import net.jejer.hipda.glide.GlideImageView;
import net.jejer.hipda.glide.ImageReadyInfo;
import net.jejer.hipda.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class ThreadDetailAdapter extends HiAdapter<DetailBean> {

    private Context mCtx;
    private LayoutInflater mInflater;
    private Button.OnClickListener mGoToFloorListener;
    private View.OnClickListener mAvatarListener;
    private ThreadDetailFragment mDetailFragment;

    private long delayAnimDeadline = 0;

    private Map<String, Map<Integer, ThreadImageLayout>> imageLayoutMap = new HashMap<>();

    public ThreadDetailAdapter(Context context, ThreadDetailFragment detailFragment,
                               Button.OnClickListener gotoFloorListener, View.OnClickListener avatarListener) {
        mCtx = context;
        mInflater = LayoutInflater.from(context);
        mGoToFloorListener = gotoFloorListener;
        mAvatarListener = avatarListener;
        mDetailFragment = detailFragment;
        delayAnimDeadline = System.currentTimeMillis() + context.getResources().getInteger(R.integer.defaultAnimTime) + 50;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DetailBean detail = getItem(position);

        ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.item_thread_detail, parent, false);

            holder = new ViewHolder();
            holder.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            holder.author = (TextView) convertView.findViewById(R.id.tv_username);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.floor = (TextView) convertView.findViewById(R.id.floor);
            holder.postStatus = (TextView) convertView.findViewById(R.id.post_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.author.setText(detail.getAuthor());
        holder.time.setText(Utils.shortyTime(detail.getTimePost()));
        holder.floor.setText(detail.getFloor() + "#");

        boolean trimBr = false;
        String postStaus = detail.getPostStatus();
        if (postStaus != null && postStaus.length() > 0) {
            holder.postStatus.setText(postStaus);
            holder.postStatus.setVisibility(View.VISIBLE);
            trimBr = true;
        } else {
            holder.postStatus.setVisibility(View.GONE);
        }

        if (HiSettingsHelper.getInstance().isLoadAvatar()) {
            holder.avatar.setVisibility(View.VISIBLE);
            loadAvatar(detail.getAvatarUrl(), holder.avatar);
        } else {
            holder.avatar.setVisibility(View.GONE);
        }
        holder.avatar.setTag(R.id.avatar_tag_uid, detail.getUid());
        holder.avatar.setTag(R.id.avatar_tag_username, detail.getAuthor());
        holder.avatar.setOnClickListener(mAvatarListener);

        holder.author.setTag(R.id.avatar_tag_uid, detail.getUid());
        holder.author.setTag(R.id.avatar_tag_username, detail.getAuthor());
        holder.author.setOnClickListener(mAvatarListener);

        LinearLayout contentView = (LinearLayout) convertView.findViewById(R.id.content_layout);
        contentView.removeAllViews();
        contentView.bringToFront();

        for (int i = 0; i < detail.getContents().getSize(); i++) {
            ContentAbs content = detail.getContents().get(i);
            if (content instanceof ContentText) {
                TextViewWithEmoticon tv = (TextViewWithEmoticon) mInflater.inflate(R.layout.item_textview_withemoticon, parent, false);
                tv.setFragment(mDetailFragment);
                tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                tv.setPadding(8, 8, 8, 8);

                //dirty hack, remove extra <br>
                String cnt = content.getContent();
                if (trimBr) {
                    if (cnt.startsWith("<br><br><br>")) {
                        cnt = cnt.substring("<br><br>".length());
                    } else if (cnt.startsWith("<br><br>")) {
                        cnt = cnt.substring("<br>".length());
                    }
                }
                if (!"<br>".equals(cnt)) {
                    tv.setText(cnt);
                    tv.setFocusable(false);
                    contentView.addView(tv);
                }
            } else if (content instanceof ContentImg) {
                final ContentImg contentImg = ((ContentImg) content);

                final String imageUrl = contentImg.getContent();
                int imageIndex = contentImg.getIndexInPage();

                final ThreadImageLayout threadImageLayout = new ThreadImageLayout(mCtx);
                final GlideImageView giv = threadImageLayout.getImageView();

                giv.setFragment(mDetailFragment);
                giv.setFocusable(false);
                giv.setClickable(true);

                Map<Integer, ThreadImageLayout> subImageMap;
                if (imageLayoutMap.containsKey(imageUrl)) {
                    subImageMap = imageLayoutMap.get(imageUrl);
                } else {
                    subImageMap = new HashMap<>();
                }
                subImageMap.put(imageIndex, threadImageLayout);
                imageLayoutMap.put(imageUrl, subImageMap);

                ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(imageUrl);

                RelativeLayout.LayoutParams params;
                if (imageReadyInfo != null && imageReadyInfo.isReady()) {
                    params = new RelativeLayout.LayoutParams(imageReadyInfo.getDisplayWidth(), imageReadyInfo.getDisplayHeight());
                } else {
                    params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(mCtx, 150));
                    giv.setImageDrawable(ContextCompat.getDrawable(mCtx, R.drawable.ic_action_image));
                }
                giv.setLayoutParams(params);
                contentView.addView(threadImageLayout);

                giv.setUrl(imageUrl);
                giv.setImageIndex(imageIndex);

                //delay images 50ms more than avatar
                long delay = delayAnimDeadline + 50 - System.currentTimeMillis();
                if (imageReadyInfo != null && imageReadyInfo.isReady()) {
                    loadImage(imageUrl, giv, delay);
                } else {
                    if (!HiSettingsHelper.getInstance().isLoadImage()) {
                        if (contentImg.getFileSize() > 0) {
                            threadImageLayout.getImageInfoTextView().setVisibility(View.VISIBLE);
                            threadImageLayout.getImageInfoTextView().setText(Utils.toSizeText(contentImg.getFileSize()));
                        }
                        giv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                GlideImageManager.addJob(new GlideImageJob(mDetailFragment, imageUrl, GlideImageManager.PRIORITY_LOW, mDetailFragment.sessionId, true));
                                giv.setOnClickListener(null);
                            }
                        });
                    }
                    GlideImageManager.addJob(new GlideImageJob(
                            mDetailFragment,
                            imageUrl,
                            GlideImageManager.PRIORITY_LOW,
                            mDetailFragment.sessionId,
                            HiSettingsHelper.getInstance().isLoadImage(),
                            delay));
                }

            } else if (content instanceof ContentAttach) {
                TextViewWithEmoticon tv = (TextViewWithEmoticon) mInflater.inflate(R.layout.item_textview_withemoticon, parent, false);
                tv.setFragment(mDetailFragment);
                tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                tv.setText(content.getContent());
                tv.setFocusable(false);
                contentView.addView(tv);
            } else if (content instanceof ContentQuote && !((ContentQuote) content).isReplyQuote()) {

                LinearLayout quoteLayout = (LinearLayout) mInflater.inflate(R.layout.item_quote_text_simple, parent, false);
                TextViewWithEmoticon tv = (TextViewWithEmoticon) quoteLayout.findViewById(R.id.quote_content);
                tv.setFragment(mDetailFragment);

                tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
                tv.setAutoLinkMask(Linkify.WEB_URLS);
                tv.setText(content.getContent());
                tv.setFocusable(false);    // make convertView long clickable.

                contentView.addView(quoteLayout);
                trimBr = true;
            } else if (content instanceof ContentGoToFloor || content instanceof ContentQuote) {

                String author = "";
                String time = "";
                String note = "";
                String text = "";

                int floor = -1;
                if (content instanceof ContentGoToFloor) {
                    //floor is not accurate if some user deleted post
                    //use floor to get page, then get cache by postid
                    ContentGoToFloor goToFloor = (ContentGoToFloor) content;
                    author = goToFloor.getAuthor();
                    floor = goToFloor.getFloor();
                    DetailBean detailBean = mDetailFragment.getCachedPost(goToFloor.getPostId());
                    if (detailBean != null) {
                        text = detailBean.getContents().getContent();
                        floor = Integer.parseInt(detailBean.getFloor());
                    }
                    note = floor + "#";
                } else {
                    ContentQuote contentQuote = (ContentQuote) content;
                    DetailBean detailBean = null;
                    if (!TextUtils.isEmpty(contentQuote.getPostId()) && TextUtils.isDigitsOnly(contentQuote.getPostId())) {
                        detailBean = mDetailFragment.getCachedPost(contentQuote.getPostId());
                    }
                    if (detailBean != null) {
                        author = contentQuote.getAuthor();
                        text = detailBean.getContents().getContent();
                        floor = Integer.parseInt(detailBean.getFloor());
                        note = floor + "#";
                    } else {
                        author = ((ContentQuote) content).getAuthor();
                        if (!TextUtils.isEmpty(((ContentQuote) content).getTo()))
                            note = "to: " + ((ContentQuote) content).getTo();
                        time = ((ContentQuote) content).getTime();
                        text = ((ContentQuote) content).getText();
                    }
                }

                LinearLayout quoteLayout = (LinearLayout) mInflater.inflate(R.layout.item_quote_text, parent, false);

                TextView tvAuthor = (TextView) quoteLayout.findViewById(R.id.quote_author);
                TextView tvNote = (TextView) quoteLayout.findViewById(R.id.quote_note);
                TextViewWithEmoticon tvContent = (TextViewWithEmoticon) quoteLayout.findViewById(R.id.quote_content);
                TextView tvTime = (TextView) quoteLayout.findViewById(R.id.quote_post_time);

                tvContent.setFragment(mDetailFragment);
                tvContent.setTrim(true);

                tvAuthor.setText(Utils.nullToText(author));
                tvNote.setText(Utils.nullToText(note));
                tvContent.setText(Utils.nullToText(text));
                tvTime.setText(Utils.nullToText(time));

                tvAuthor.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 2);
                tvNote.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 2);
                tvContent.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
                tvTime.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 4);

                if (floor > 0) {
                    tvNote.setTag(floor);
                    tvNote.setOnClickListener(mGoToFloorListener);
                    tvNote.setFocusable(false);
                    tvNote.setClickable(true);
                }

                contentView.addView(quoteLayout);
                trimBr = true;
            }
        }

        return convertView;
    }

    private void loadImage(final String imageUrl, final GlideImageView giv, long delay) {
        if (delay > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDetailFragment.loadImage(imageUrl, giv);
                }
            }, delay);
        } else {
            mDetailFragment.loadImage(imageUrl, giv);
        }
    }

    private void loadAvatar(final String avatarUrl, final ImageView imageView) {
        long delay = delayAnimDeadline - System.currentTimeMillis();
        if (delay > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    GlideHelper.loadAvatar(mDetailFragment, imageView, avatarUrl);
                }
            }, delay);
        } else {
            GlideHelper.loadAvatar(mDetailFragment, imageView, avatarUrl);
        }
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(GlideImageEvent event) {
        String imageUrl = event.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)
                && imageLayoutMap.containsKey(imageUrl)) {
            Map<Integer, ThreadImageLayout> subImageMap = imageLayoutMap.get(imageUrl);
            for (ThreadImageLayout layout : subImageMap.values()) {
                ProgressBar bar = layout.getProgressBar();
                if (ViewCompat.isAttachedToWindow(layout)) {
                    if (event.isInProgress()) {
                        if (bar.getVisibility() != View.VISIBLE)
                            bar.setVisibility(View.VISIBLE);
                        bar.setProgress(event.getProgress());
                    } else {
                        if (bar.getVisibility() == View.VISIBLE)
                            bar.setVisibility(View.GONE);
                        TextView imageInfo = layout.getImageInfoTextView();
                        GlideImageView giv = layout.getImageView();
                        mDetailFragment.loadImage(imageUrl, giv);
                        if (imageInfo.getVisibility() == View.VISIBLE) {
                            imageInfo.setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    private static class ViewHolder {
        ImageView avatar;
        TextView author;
        TextView floor;
        TextView postStatus;
        TextView time;
    }
}
