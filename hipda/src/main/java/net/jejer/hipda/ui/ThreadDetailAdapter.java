package net.jejer.hipda.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.glide.GlideScaleViewTarget;
import net.jejer.hipda.glide.ThreadImageDecoder;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ThreadDetailAdapter extends ArrayAdapter<DetailBean> {

    private Context mCtx;
    private LayoutInflater mInflater;
    private Button.OnClickListener mGoToFloorListener;
    private View.OnClickListener mAvatarListener;
    private FragmentManager mFragmentManager;
    private ThreadDetailFragment mDetailFragment;

    private List<String> loadedImages = new ArrayList<String>();

    public ThreadDetailAdapter(Context context, FragmentManager fm, ThreadDetailFragment detailFragment, int resource,
                               List<DetailBean> objects, Button.OnClickListener gotoFloorListener, View.OnClickListener avatarListener) {
        super(context, resource, objects);
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

        float lineSpacingExtra = 1f;
        float lineSpacingMultiplier = 1.0f;
        if (HiSettingsHelper.getInstance().getPostLineSpacing() == 1) {
            lineSpacingExtra = 2;
            lineSpacingMultiplier = 1.1f;
        } else if (HiSettingsHelper.getInstance().getPostLineSpacing() == 2) {
            lineSpacingExtra = 4;
            lineSpacingMultiplier = 1.2f;
        } else if (HiSettingsHelper.getInstance().getPostLineSpacing() == 3) {
            lineSpacingExtra = 6;
            lineSpacingMultiplier = 1.3f;
        }

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
            GlideHelper.loadAvatar(getContext(), holder.avatar, detail.getAvatarUrl());
        } else {
            holder.avatar.setVisibility(View.GONE);
        }
        holder.avatar.setTag(R.id.avatar_tag_uid, detail.getUid());
        holder.avatar.setTag(R.id.avatar_tag_username, detail.getAuthor());
        holder.avatar.setOnClickListener(mAvatarListener);


//should be useless!
//        holder.author.setTag(R.id.avatar_tag_uid, detail.getUid());
//        holder.author.setTag(R.id.avatar_tag_username, detail.getAuthor());
//        holder.author.setOnClickListener(mAvatarListener);

        LinearLayout contentView = (LinearLayout) convertView.findViewById(R.id.content_layout);
        contentView.removeAllViews();
        for (int i = 0; i < detail.getContents().getSize(); i++) {
            ContentAbs content = detail.getContents().get(i);
            if (content instanceof ContentText) {
                TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
                tv.setFragmentManager(mFragmentManager);
                tv.setTextSize(HiSettingsHelper.getPostTextSize());
                tv.setPadding(8, 8, 8, 8);
                if (HiSettingsHelper.getInstance().getPostLineSpacing() > 0) {
                    tv.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier);
                }

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
                    //setAutoLinkMask have conflict with setMovementMethod
                    //tv.setAutoLinkMask(Linkify.WEB_URLS);
                    tv.setFocusable(false);
                    contentView.addView(tv);
                }
            } else if (content instanceof ContentImg) {
                final String imageUrl = content.getContent();

                final TextView textView = new TextView(mCtx);
                textView.setBackgroundColor(mCtx.getResources().getColor(R.color.background_silver));
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setVisibility(View.INVISIBLE);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 400);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                final GlideImageView giv = new GlideImageView(mCtx);
                giv.setFocusable(false);
                giv.setClickable(true);
                giv.setLayoutParams(params);

                textView.setClickable(true);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        giv.performClick();
                    }
                });

                contentView.addView(giv);
                contentView.addView(textView);

                giv.setUrl(imageUrl);

                if (HiUtils.isAutoLoadImg(mCtx) || loadedImages.contains(imageUrl)) {
                    loadImage(imageUrl, textView, giv);
                } else {
                    giv.setImageResource(R.drawable.ic_action_picture);
                    giv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            loadedImages.add(imageUrl);
                            giv.setImageResource(R.drawable.loading);
                            loadImage(imageUrl, textView, giv);
                        }
                    });
                }

            } else if (content instanceof ContentAttach) {
                TextViewWithEmoticon tv = new TextViewWithEmoticon(mCtx);
                tv.setFragmentManager(mFragmentManager);
                tv.setTextSize(HiSettingsHelper.getPostTextSize());
                tv.setText(content.getContent());
                tv.setFocusable(false);
                contentView.addView(tv);
            } else if (content instanceof ContentQuote && !((ContentQuote) content).isReplyQuote()) {

                TypedValue typedValue = new TypedValue();
                mCtx.getTheme().resolveAttribute(R.attr.quote_text_background, typedValue, true);
                int colorRscId = typedValue.resourceId;

                TextView tv = new TextView(mCtx);
                tv.setTextSize(HiSettingsHelper.getPostTextSize() - 1);
                tv.setAutoLinkMask(Linkify.WEB_URLS);
                tv.setText(content.getContent());
                tv.setFocusable(false);    // make convertView long clickable.
                tv.setPadding(16, 16, 16, 16);
                tv.setBackgroundColor(mCtx.getResources().getColor(colorRscId));
                if (HiSettingsHelper.getInstance().getPostLineSpacing() > 0) {
                    tv.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier);
                }
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
                    DetailBean detailBean = mDetailFragment.getCachedPost(goToFloor.getFloor(), goToFloor.getPostId());
                    if (detailBean != null) {
                        text = detailBean.getContents().getCopyText(true);
                        floor = Integer.parseInt(detailBean.getFloor());
                    }
                    note = floor + "#";
                } else {
                    author = ((ContentQuote) content).getAuthor();
                    if (!TextUtils.isEmpty(((ContentQuote) content).getTo()))
                        note = "to: " + ((ContentQuote) content).getTo();
                    time = ((ContentQuote) content).getTime();
                    text = ((ContentQuote) content).getText();
                }

                LinearLayout quoteLayout = (LinearLayout) LayoutInflater.from(mCtx)
                        .inflate(R.layout.item_quote_text, parent, false);

                TextView tvAuthor = (TextView) quoteLayout.findViewById(R.id.quote_author);
                TextView tvNote = (TextView) quoteLayout.findViewById(R.id.quote_note);
                TextView tvContent = (TextView) quoteLayout.findViewById(R.id.quote_content);
                TextView tvTime = (TextView) quoteLayout.findViewById(R.id.quote_post_time);

                tvAuthor.setText(Utils.nullToText(author));
                tvNote.setText(Utils.nullToText(note));
                tvContent.setText(Utils.nullToText(text));
                tvTime.setText(Utils.nullToText(time));

                tvAuthor.setTextSize(HiSettingsHelper.getPostTextSize() - 2);
                tvNote.setTextSize(HiSettingsHelper.getPostTextSize() - 2);
                tvContent.setTextSize(HiSettingsHelper.getPostTextSize() - 1);
                tvTime.setTextSize(HiSettingsHelper.getPostTextSize() - 4);

                if (HiSettingsHelper.getInstance().getPostLineSpacing() > 0) {
                    tvContent.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier);
                }

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

    private void loadImage(String imageUrl, TextView textView, GlideImageView giv) {
        int maxViewWidth = 1080;
        //this fragment could be replaced by UserinfoFragment, so DO NOT cast it
        Fragment fragment = mFragmentManager.findFragmentByTag(ThreadDetailFragment.class.getName());
        if (fragment != null && fragment.getView() != null) {
            maxViewWidth = fragment.getView().getWidth();
        }
        if (imageUrl.toLowerCase().endsWith(".gif")) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_action_picture)
                    .error(R.drawable.tapatalk_image_broken)
                    .into(new GlideScaleViewTarget(mCtx, giv, textView, maxViewWidth, imageUrl));
        } else {
            Glide.with(getContext())
                    .load(imageUrl)
                    .asBitmap()
                    .cacheDecoder(new FileToStreamDecoder<Bitmap>(new ThreadImageDecoder()))
                    .imageDecoder(new ThreadImageDecoder())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_action_picture)
                    .error(R.drawable.tapatalk_image_broken)
                    .into(new GlideScaleViewTarget(mCtx, giv, textView, maxViewWidth, imageUrl));
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
