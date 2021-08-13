package net.jejer.hipda.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.SparseArray;
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
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.cache.ImageContainer;
import net.jejer.hipda.glide.GlideHelper;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.ui.widget.TextViewWithEmoticon;
import net.jejer.hipda.ui.widget.ThreadImageLayout;
import net.jejer.hipda.utils.ColorHelper;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.UIUtils;
import net.jejer.hipda.utils.Utils;

import java.util.List;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by GreenSkinMonster on 2016-11-08.
 */

public class ThreadDetailAdapter extends BaseRvAdapter<DetailBean> {

    private final Context mCtx;
    private final LayoutInflater mInflater;
    private final Button.OnClickListener mGoToFloorListener;
    private final View.OnClickListener mAvatarListener;
    private final View.OnClickListener mWarningListener;
    private final ThreadDetailFragment mDetailFragment;
    private final int mBackgroundResource;
    private final int mBackgroundColor;

    final private SparseArray<DetailListBean> mThreadPages = new SparseArray<>();
    private int mDataSize = 0;

    public ThreadDetailAdapter(Context context,
                               ThreadDetailFragment detailFragment,
                               RecyclerItemClickListener itemClickListener,
                               Button.OnClickListener gotoFloorListener,
                               View.OnClickListener avatarListener,
                               View.OnClickListener warningListener) {
        mCtx = context;
        mInflater = LayoutInflater.from(context);
        mItemClickListener = itemClickListener;
        mGoToFloorListener = gotoFloorListener;
        mAvatarListener = avatarListener;
        mWarningListener = warningListener;
        mDetailFragment = detailFragment;

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = mCtx.obtainStyledAttributes(attrs);
        mBackgroundResource = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        mBackgroundColor = UIUtils.isDayTheme(context)
                ? R.color.md_green_50 : R.color.md_blue_grey_900;
    }

    @Override
    public int getItemCount() {
        return mDataSize + getHeaderCount() + getFooterCount();
    }

    public int getDataCount() {
        return mDataSize;
    }

    private void cacheDataSize() {
        int size = 0;
        for (int i = 0; i < mThreadPages.size(); i++) {
            DetailListBean detailBeans = mThreadPages.get(mThreadPages.keyAt(i));
            size += detailBeans.getCount();
        }
        mDataSize = size;
    }

    @Override
    public void setDatas(List<DetailBean> datas) {
        throw new RuntimeException("Don't set datas here");
    }

    public void addDatas(DetailListBean detailListBean) {
        final int page = detailListBean.getPage();
        if (mThreadPages.size() == 0) {
            mThreadPages.put(page, detailListBean);
            cacheDataSize();
            notifyItemRangeInserted(getHeaderCount(), detailListBean.getCount());
            Logger.e("page range " + 0 + " - " + 0 + ", insert new page " + page);
            return;
        }
        int firstPage = mThreadPages.keyAt(0);
        int lastPage = mThreadPages.keyAt(mThreadPages.size() - 1);
        if (page == firstPage - 1) {
            mThreadPages.put(page, detailListBean);
            cacheDataSize();
            notifyItemRangeInserted(getHeaderCount(), detailListBean.getCount());
            Logger.e("page range " + firstPage + " - " + lastPage + ", insert new page " + page);
        } else if (page == lastPage + 1) {
            final int startPos = getItemCount() - getFooterCount();
            mThreadPages.put(page, detailListBean);
            cacheDataSize();
            notifyItemRangeInserted(startPos, detailListBean.getCount());
            Logger.e("page range " + firstPage + " - " + lastPage + ", append new page " + page);
        } else if (page >= firstPage && page <= lastPage) {
            if (mThreadPages.get(page) == detailListBean) {
                Logger.e("page range " + firstPage + " - " + lastPage + ", same skip exist page " + page);
            } else {
                mThreadPages.put(page, detailListBean);
                cacheDataSize();
                int pos = getHeaderCount();
                for (int i = 0; i < mThreadPages.size(); i++) {
                    int tpage = mThreadPages.keyAt(i);
                    if (tpage < page) {
                        pos += mThreadPages.get(tpage).getCount();
                    } else {
                        break;
                    }
                }
                final int startPos = pos;
                notifyItemRangeChanged(startPos, detailListBean.getCount());
                Logger.e("page range " + firstPage + " - " + lastPage + ", update exist page " + page);
            }
        } else {
            mThreadPages.clear();
            mThreadPages.put(page, detailListBean);
            cacheDataSize();
            notifyDataSetChanged();
            Logger.e("page range " + firstPage + " - " + lastPage + ", not continoius page " + page + ", CLEAR ALL");
        }
    }

    @Override
    public List<DetailBean> getDatas() {
        throw new RuntimeException("Don't set datas here");
    }

    @Override
    public DetailBean getItem(int position) {
        int pos = position - getHeaderCount();
        if (pos < 0)
            return null;
        for (int i = 0; i < mThreadPages.size(); i++) {
            DetailListBean detailBeans = mThreadPages.get(mThreadPages.keyAt(i));
            if (pos < detailBeans.getCount()) {
                return detailBeans.getAll().get(pos);
            }
            pos -= detailBeans.getCount();
            if (pos < 0)
                return null;
        }
        return null;
    }

    public int getPositionByFloor(int floor) {
        for (int i = 0; i < getItemCount(); i++) {
            DetailBean bean = getItem(i);
            if (bean != null && bean.getFloor() == floor) {
                return i;
            }
        }
        return -1;
    }

    public int getPositionByPostId(String postId) {
        for (int i = 0; i < getItemCount(); i++) {
            DetailBean bean = getItem(i);
            if (bean != null && bean.getPostId().equals(postId)) {
                return i;
            }
        }
        return -1;
    }

    public void clear() {
        removeFooterView();
        removeHeaderView();
        mThreadPages.clear();
        cacheDataSize();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolderImpl onCreateViewHolderImpl(ViewGroup parent, int position) {
        return new ViewHolderImpl(mInflater.inflate(R.layout.item_thread_detail, parent, false));
    }

    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, final int position) {
        ViewHolderImpl holder;
        if (viewHolder instanceof ViewHolderImpl)
            holder = (ViewHolderImpl) viewHolder;
        else return;

        viewHolder.itemView.setTag(position);
        viewHolder.itemView.setOnTouchListener(mItemClickListener);

        final DetailBean detail = getItem(position);

        if (detail.isHighlightMode()) {
            viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(mCtx, mBackgroundColor));
        } else {
            viewHolder.itemView.setBackgroundResource(mBackgroundResource);
        }

        holder.author.setText(detail.getAuthor());
        holder.time.setText(Utils.shortyTime(detail.getTimePost()));

        if (detail.isSelectMode()) {
            holder.floor.setText("X");
            holder.floor.setTag(null);
            holder.floor.setTextColor(ContextCompat.getColor(mCtx, R.color.md_amber_900));
            holder.floor.setClickable(false);
            holder.floor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detail.setSelectMode(false);
                    notifyItemChanged(position);
                }
            });
        } else if (detail.isWarned()) {
            holder.floor.setText(detail.getFloor() + "");
            holder.floor.setTag(detail.getUid());
            holder.floor.setTextColor(ContextCompat.getColor(mCtx, R.color.md_amber_900));
            holder.floor.setClickable(true);
            holder.floor.setOnClickListener(mWarningListener);
        } else {
            holder.floor.setText(detail.getFloor() + "");
            holder.floor.setTag(null);
            holder.floor.setTextColor(ColorHelper.getTextColorSecondary(mCtx));
            holder.floor.setClickable(false);
            holder.floor.setOnClickListener(null);
        }

        boolean trimBr = false;
        String postStaus = detail.getPostStatus();
        if (postStaus != null && postStaus.length() > 0) {
            holder.postStatus.setText(Utils.shortyTime(postStaus));
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

        LinearLayout contentView = holder.contentView;
        contentView.removeAllViews();
        contentView.bringToFront();

        if (detail.isSelectMode()) {
            AppCompatTextView tv = (AppCompatTextView) mInflater.inflate(R.layout.item_textview, null, false);
            tv.setText(detail.getContents().getCopyText());
            tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
            tv.setPadding(8, 8, 8, 8);
            UIUtils.setLineSpacing(tv);
            tv.setTextIsSelectable(true);

            contentView.addView(tv);
        } else {
            for (int i = 0; i < detail.getContents().getSize(); i++) {
                ContentAbs content = detail.getContents().get(i);
                if (content instanceof ContentText) {
                    TextViewWithEmoticon tv = (TextViewWithEmoticon) mInflater.inflate(R.layout.item_textview_withemoticon, null, false);
                    tv.setFragment(mDetailFragment);
                    tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                    tv.setPadding(8, 8, 8, 8);

                    String cnt = content.getContent();
                    if (trimBr)
                        cnt = Utils.removeLeadingBlank(cnt);
                    if (!TextUtils.isEmpty(cnt)) {
                        tv.setText(cnt);
                        tv.setFocusable(false);
                        contentView.addView(tv);
                    }
                } else if (content instanceof ContentImg) {
                    final ContentImg contentImg = ((ContentImg) content);

                    String policy = HiSettingsHelper.getInstance().getCurrectImagePolicy();
                    String thumbUrl = contentImg.getThumbUrl();
                    String fullUrl = contentImg.getContent();
                    boolean mIsThumb;
                    if (HiSettingsHelper.IMAGE_POLICY_ORIGINAL.equals(policy)
                            || TextUtils.isEmpty(thumbUrl)
                            || fullUrl.equals(thumbUrl)
                            || ImageContainer.getImageInfo(fullUrl).isSuccess()) {
                        mIsThumb = false;
                    } else {
                        mIsThumb = true;
                    }

                    ThreadImageLayout threadImageLayout = new ThreadImageLayout(mDetailFragment.getActivity(), contentImg, mDetailFragment.getImagesInPage(detail.getPage()), mIsThumb);

                    contentView.addView(threadImageLayout);
                } else if (content instanceof ContentAttach) {
                    TextViewWithEmoticon tv = (TextViewWithEmoticon) mInflater.inflate(R.layout.item_textview_withemoticon, null, false);
                    tv.setFragment(mDetailFragment);
                    tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize());
                    tv.setText(content.getContent());
                    tv.setFocusable(false);
                    contentView.addView(tv);
                } else if (content instanceof ContentQuote && !((ContentQuote) content).isReplyQuote()) {

                    LinearLayout quoteLayout = (LinearLayout) mInflater.inflate(R.layout.item_quote_text_simple, null, false);
                    TextViewWithEmoticon tv = quoteLayout.findViewById(R.id.quote_content);
                    tv.setFragment(mDetailFragment);

                    tv.setTextSize(HiSettingsHelper.getInstance().getPostTextSize() - 1);
                    tv.setAutoLinkMask(Linkify.WEB_URLS);
                    tv.setText(Utils.removeLeadingBlank(content.getContent()));
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
                        String postId = goToFloor.getPostId();
                        DetailBean detailBean = mDetailFragment.getCachedPost(postId);
                        if (detailBean != null) {
                            text = detailBean.getContents().getContent();
                            floor = detailBean.getFloor();
                        }
                        note = floor + "#";
                    } else {
                        ContentQuote contentQuote = (ContentQuote) content;
                        DetailBean detailBean = null;
                        String postId = contentQuote.getPostId();
                        if (HiUtils.isValidId(postId)) {
                            detailBean = mDetailFragment.getCachedPost(postId);
                        }
                        if (detailBean != null) {
                            author = contentQuote.getAuthor();
                            text = detailBean.getContents().getContent();
                            floor = detailBean.getFloor();
                            note = floor + "#";
                        } else {
                            author = contentQuote.getAuthor();
                            if (!TextUtils.isEmpty(contentQuote.getTo()))
                                note = "to: " + contentQuote.getTo();
                            time = contentQuote.getTime();
                            text = contentQuote.getText();
                        }
                    }

                    text = Utils.removeLeadingBlank(text);

                    ConstraintLayout quoteLayout = (ConstraintLayout) mInflater.inflate(R.layout.item_quote_text, null, false);

                    TextView tvAuthor = quoteLayout.findViewById(R.id.quote_author);
                    TextView tvNote = quoteLayout.findViewById(R.id.quote_note);
                    TextViewWithEmoticon tvContent = quoteLayout.findViewById(R.id.quote_content);
                    TextView tvTime = quoteLayout.findViewById(R.id.quote_post_time);

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
                        tvAuthor.setTag(floor);
                        tvAuthor.setOnClickListener(mGoToFloorListener);
                        tvAuthor.setFocusable(false);
                        tvAuthor.setClickable(true);
                    }

                    contentView.addView(quoteLayout);
                    trimBr = true;
                }
            }
        }
    }

    private void loadAvatar(final String avatarUrl, final ImageView imageView) {
        GlideHelper.loadAvatar(mDetailFragment, imageView, avatarUrl);
    }

    private static class ViewHolderImpl extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView author;
        TextView floor;
        TextView postStatus;
        TextView time;
        LinearLayout contentView;

        ViewHolderImpl(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            author = itemView.findViewById(R.id.tv_username);
            time = itemView.findViewById(R.id.time);
            floor = itemView.findViewById(R.id.floor);
            postStatus = itemView.findViewById(R.id.post_status);
            contentView = itemView.findViewById(R.id.content_layout);
        }
    }

}
