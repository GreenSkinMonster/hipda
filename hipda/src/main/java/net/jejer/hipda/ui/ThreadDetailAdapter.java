package net.jejer.hipda.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;

import net.jejer.hipda.R;
import net.jejer.hipda.bean.ContentAbs;
import net.jejer.hipda.bean.ContentAttach;
import net.jejer.hipda.bean.ContentGoToFloor;
import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.ContentQuote;
import net.jejer.hipda.bean.ContentText;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.LRUCache;
import net.jejer.hipda.glide.GifTransformation;
import net.jejer.hipda.glide.GlideFutureTask;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideImageView;
import net.jejer.hipda.glide.ImageContainer;
import net.jejer.hipda.glide.ImageReadyInfo;
import net.jejer.hipda.glide.ThreadImageDecoder;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Utils;

public class ThreadDetailAdapter extends HiAdapter<DetailBean> implements ImageContainer {

    private Context mCtx;
    private LayoutInflater mInflater;
    private Button.OnClickListener mGoToFloorListener;
    private View.OnClickListener mAvatarListener;
    private FragmentManager mFragmentManager;
    private ThreadDetailFragment mDetailFragment;

    private static LRUCache<String, ImageReadyInfo> loadedImages = new LRUCache<>(1024);

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
            holder.author = (TextView) convertView.findViewById(R.id.author);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            holder.floor = (TextView) convertView.findViewById(R.id.floor);
            holder.postStatus = (TextView) convertView.findViewById(R.id.post_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.author.setText(detail.getAuthor());
        holder.time.setText(detail.getTimePost());
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

        if (HiSettingsHelper.getInstance().isShowThreadListAvatar()) {
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

        for (int i = 0; i < detail.getContents().getSize(); i++) {
            ContentAbs content = detail.getContents().get(i);
            if (content instanceof ContentText) {
                TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
                tv.setFragmentManager(mFragmentManager);
                tv.setTextSize(HiSettingsHelper.getPostTextSize());
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

                final GlideImageView giv = new GlideImageView(mCtx);
                giv.setFocusable(false);
                giv.setClickable(true);

                ImageReadyInfo imageReadyInfo = loadedImages.get(imageUrl);

                LinearLayout.LayoutParams params;
                if (imageReadyInfo != null && imageReadyInfo.isReady()) {
                    params = new LinearLayout.LayoutParams(imageReadyInfo.getWidth(), imageReadyInfo.getHeight());
                    giv.setBackgroundColor(mCtx.getResources().getColor(R.color.background_silver));
                } else {
                    params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);
                    giv.setImageResource(R.drawable.ic_action_picture);
                }
                giv.setLayoutParams(params);
                contentView.addView(giv);

                giv.setUrl(imageUrl);

                if (imageReadyInfo != null && imageReadyInfo.isReady()) {
                    loadImage(imageUrl, giv);
                } else {
                    final int maxViewWidth;
                    //this fragment could be replaced by UserinfoFragment, so DO NOT cast it
                    Fragment fragment = mFragmentManager.findFragmentByTag(ThreadDetailFragment.class.getName());
                    if (fragment != null && fragment.getView() != null) {
                        maxViewWidth = fragment.getView().getWidth();
                    } else {
                        maxViewWidth = 1080;
                    }

                    if (HiUtils.isAutoLoadImg(mCtx)) {
                        new GlideFutureTask(mCtx, this, giv, maxViewWidth, imageUrl).execute();
                    } else {
                        giv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                giv.setImageResource(R.drawable.loading);
                                new GlideFutureTask(mCtx, ThreadDetailAdapter.this, giv, maxViewWidth, imageUrl).execute();
                                giv.setOnClickListener(null);
                            }
                        });
                    }
                }

            } else if (content instanceof ContentAttach) {
                TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
                tv.setFragmentManager(mFragmentManager);
                tv.setTextSize(HiSettingsHelper.getPostTextSize());
                tv.setText(content.getContent());
                tv.setFocusable(false);
                contentView.addView(tv);
            } else if (content instanceof ContentQuote && !((ContentQuote) content).isReplyQuote()) {

                TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
                tv.setTextSize(HiSettingsHelper.getPostTextSize() - 1);
                tv.setAutoLinkMask(Linkify.WEB_URLS);
                tv.setText(content.getContent());
                tv.setFocusable(false);    // make convertView long clickable.
                tv.setPadding(16, 16, 16, 16);
                tv.setBackgroundColor(mCtx.getResources().getColor(HiSettingsHelper.getInstance().getQuoteTextBackgroundColor()));

                contentView.addView(tv);
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

                LinearLayout quoteLayout = (LinearLayout) LayoutInflater.from(mCtx)
                        .inflate(R.layout.item_quote_text, parent, false);

                TextView tvAuthor = (TextView) quoteLayout.findViewById(R.id.quote_author);
                TextView tvNote = (TextView) quoteLayout.findViewById(R.id.quote_note);
                TextViewWithEmoticon tvContent = (TextViewWithEmoticon) quoteLayout.findViewById(R.id.quote_content);
                TextView tvTime = (TextView) quoteLayout.findViewById(R.id.quote_post_time);

                tvContent.setTrim(true);

                tvAuthor.setText(Utils.nullToText(author));
                tvNote.setText(Utils.nullToText(note));
                tvContent.setText(Utils.nullToText(text));
                tvTime.setText(Utils.nullToText(time));

                tvAuthor.setTextSize(HiSettingsHelper.getPostTextSize() - 2);
                tvNote.setTextSize(HiSettingsHelper.getPostTextSize() - 2);
                tvContent.setTextSize(HiSettingsHelper.getPostTextSize() - 1);
                tvTime.setTextSize(HiSettingsHelper.getPostTextSize() - 4);

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

    public void loadImage(String imageUrl, GlideImageView giv) {

        if (mCtx == null)
            return;
        if (Build.VERSION.SDK_INT >= 17
                && (mCtx instanceof Activity)
                && ((Activity) mCtx).isDestroyed())
            return;
        if (!mDetailFragment.isAdded() || mDetailFragment.isDetached())
            return;

        ImageReadyInfo imageReadyInfo = loadedImages.get(imageUrl);

        if (imageReadyInfo != null && imageReadyInfo.isReady()) {
            giv.getLayoutParams().width = imageReadyInfo.getWidth();
            giv.getLayoutParams().height = imageReadyInfo.getHeight();
            if (imageReadyInfo.getWidth() > GlideImageView.MIN_SCALE_WIDTH || imageReadyInfo.isGif()) {
                giv.setImageReadyInfo(imageReadyInfo);
                giv.setClickToViewBigImage();
            }

            if (imageReadyInfo.isGif()) {
                Glide.with(mCtx)
                        .load(imageUrl)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new GifTransformation(mCtx))
                        .override(imageReadyInfo.getWidth(), imageReadyInfo.getHeight())
                        .into(giv);
            } else {
                Glide.with(mCtx)
                        .load(imageUrl)
                        .asBitmap()
                        .cacheDecoder(new FileToStreamDecoder<>(new ThreadImageDecoder()))
                        .imageDecoder(new ThreadImageDecoder())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(imageReadyInfo.getWidth(), imageReadyInfo.getHeight())
                        .into(giv);
            }
        } else {
            giv.setImageResource(R.drawable.tapatalk_image_broken);
        }
    }

    @Override
    public void markImageReady(String url, ImageReadyInfo imageReadyInfo) {
        loadedImages.put(url, imageReadyInfo);
    }

    private static class ViewHolder {
        ImageView avatar;
        TextView author;
        TextView floor;
        TextView postStatus;
        TextView time;
    }
}
