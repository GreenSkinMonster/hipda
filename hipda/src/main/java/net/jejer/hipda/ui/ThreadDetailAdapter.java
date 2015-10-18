package net.jejer.hipda.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class ThreadDetailAdapter extends HiAdapter<DetailBean> {

    private Context mCtx;
    private LayoutInflater mInflater;
    private Button.OnClickListener mGoToFloorListener;
    private View.OnClickListener mAvatarListener;
    private FragmentManager mFragmentManager;
    private ThreadDetailFragment mDetailFragment;

    public ThreadDetailAdapter(Context context, FragmentManager fm, ThreadDetailFragment detailFragment,
                               Button.OnClickListener gotoFloorListener, View.OnClickListener avatarListener) {
        mCtx = context;
        mFragmentManager = fm;
        mInflater = LayoutInflater.from(context);
        mGoToFloorListener = gotoFloorListener;
        mAvatarListener = avatarListener;
        mDetailFragment = detailFragment;
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
            GlideHelper.loadAvatar(mCtx, holder.avatar, detail.getAvatarUrl());
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
                tv.setFragmentManager(mFragmentManager);
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
                final String imageUrl = content.getContent();

                final GlideImageView giv = new GlideImageView(mCtx, mDetailFragment);
                giv.setFocusable(false);
                giv.setClickable(true);

                ImageReadyInfo imageReadyInfo = ImageContainer.getImageInfo(imageUrl);

                LinearLayout.LayoutParams params;
                if (imageReadyInfo != null && imageReadyInfo.isReady()) {
                    params = new LinearLayout.LayoutParams(imageReadyInfo.getWidth(), imageReadyInfo.getHeight());
                    giv.setBackgroundColor(mCtx.getResources().getColor(R.color.background_silver));
                } else {
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);
                }
                giv.setLayoutParams(params);
                contentView.addView(giv);

                giv.setUrl(imageUrl);
                giv.setImageIndex(((ContentImg) content).getIndexInPage());
                giv.bringToFront();

                if (imageReadyInfo != null && imageReadyInfo.isReady()) {
                    giv.setImageDrawable(GlideHelper.getImageDownloadHolder(mCtx));
                    mDetailFragment.loadImage(imageUrl, giv);
                } else {
                    if (HiSettingsHelper.getInstance().isLoadImage()) {
                        giv.setImageDrawable(GlideHelper.getImageDownloadHolder(mCtx));
                        GlideImageManager.getInstance().addJob(new GlideImageJob(mCtx, imageUrl, 1, giv));
                    } else {
                        giv.setImageDrawable(GlideHelper.getImageManualHolder(mCtx));
                        giv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                giv.setImageDrawable(GlideHelper.getImageDownloadHolder(mCtx));
                                GlideImageManager.getInstance().addJob(new GlideImageJob(mCtx, imageUrl, 1, giv));
                                giv.setOnClickListener(null);
                            }
                        });
                    }
                }

            } else if (content instanceof ContentAttach) {
                TextViewWithEmoticon tv = (TextViewWithEmoticon) mInflater.inflate(R.layout.item_textview_withemoticon, parent, false);
                tv.setFragmentManager(mFragmentManager);
                tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                tv.setText(content.getContent());
                tv.setFocusable(false);
                contentView.addView(tv);
            } else if (content instanceof ContentQuote && !((ContentQuote) content).isReplyQuote()) {

                LinearLayout quoteLayout = (LinearLayout) mInflater.inflate(R.layout.item_quote_text_simple, parent, false);
                TextViewWithEmoticon tv = (TextViewWithEmoticon) quoteLayout.findViewById(R.id.quote_content);
                tv.setFragmentManager(mFragmentManager);

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

                tvContent.setFragmentManager(mFragmentManager);
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

    @SuppressWarnings("unused")
    public void onEventMainThread(GlideImageEvent event) {
        if (event.getImageUrl() != null
                && !TextUtils.isEmpty(event.getImageUrl())
                && event.getView() instanceof GlideImageView) {
            GlideImageView giv = (GlideImageView) event.getView();
            mDetailFragment.loadImage(event.getImageUrl(), giv);
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
