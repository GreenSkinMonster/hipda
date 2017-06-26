package net.jejer.hipda.bean;

import net.jejer.hipda.ui.textstyle.TextStyle;
import net.jejer.hipda.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;

public class DetailBean {
    private String mAuthor;
    private String mUid;
    private String mAvatarUrl;
    private String mPostId;
    private String mTimePost;
    private int mFloor;
    private String mPostStatus;
    private Contents mContents;
    private int mPage;
    private boolean mWarned;
    private Collection<ContentImg> mImages = new ArrayList<>();
    private boolean mSelectMode;
    private boolean mHighlightMode;

    public DetailBean() {
        mContents = new Contents();
    }

    public class Contents {
        private ArrayList<ContentAbs> list;
        private int lastTextIdx;
        private Boolean newString;

        public Contents() {
            list = new ArrayList<>();
            lastTextIdx = -1;
            newString = true;
        }

        public void addText(String text) {
            addText(text, null);
        }

        public void addText(String text, TextStyle textStyle) {
            if (textStyle != null)
                text = textStyle.toHtml(text);
            if (newString) {
                ContentText ct = new ContentText(text);
                list.add(ct);
                lastTextIdx = list.size() - 1;
                newString = false;
            } else {
                ContentText ct = (ContentText) list.get(lastTextIdx);
                ct.append(text);
            }
        }

        public void addNotice(String text) {
            TextStyle ts = new TextStyle();
            ts.setColor("Gray");
            text = ts.toHtml(text);
            addText(text, ts);
        }

        public void addAppMark(String text, String url) {
            if (!HiSettingsHelper.getInstance().isShowTail()
                    && !HiSettingsHelper.getInstance().getUid().equals(mUid))
                return;
            String mark;
            if (url != null && url.length() > 0) {
                mark = "<appmark><a href=\"" + url + "\">" + text + "</a></appmark>";
            } else {
                mark = "<appmark>" + text + "</appmark>";
            }
            if (newString) {
                list.add(new ContentText(mark));
                lastTextIdx = list.size() - 1;
                newString = false;
            } else {
                ContentText ct = (ContentText) list.get(lastTextIdx);
                ct.append(mark);
            }
        }

        public void addLink(String text, String url) {
            String link;
            if (!url.toLowerCase().startsWith("http://")
                    && !url.toLowerCase().startsWith("https://")) {
                url = "http://" + url;
                link = " <a href=\"" + url + "\">" + text + "</a> ";
            } else {
                link = "[<a href=\"" + url + "\">" + text + "</a>]";
            }
            if (newString) {
                list.add(new ContentText(link));
                lastTextIdx = list.size() - 1;
                newString = false;
            } else {
                ContentText ct = (ContentText) list.get(lastTextIdx);
                ct.append(link);
            }
        }

        public void addEmail(String email) {
            String link = " <a href=\"mailto:" + email + "\">" + email + "</a> ";
            if (newString) {
                list.add(new ContentText(link));
                lastTextIdx = list.size() - 1;
                newString = false;
            } else {
                ContentText ct = (ContentText) list.get(lastTextIdx);
                ct.append(link);
            }
        }

        public void addImg(String url) {
            addImg(new ContentImg(url, 0, ""));
        }

        public void addImg(ContentImg contentImg) {
            list.add(contentImg);
            mImages.add(contentImg);
            newString = true;
        }

        public void addAttach(String url, String title, String desc) {
            list.add(new ContentAttach(url, unEscapeHtml(title), desc));
            newString = true;
        }

        public void addQuote(String text, String authorAndTime, String tid, String postId) {
            list.add(new ContentQuote(text, authorAndTime, tid, postId));
            newString = true;
        }

        public void addGoToFloor(String text, String tid, String postId, int floor, String author) {
            list.add(new ContentGoToFloor(text, tid, postId, floor, author));
            newString = true;
        }

        public int getSize() {
            return list.size();
        }

        public ContentAbs get(int idx) {
            return list.get(idx);
        }

        public String getCopyText() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                ContentAbs o = list.get(i);
                if (o instanceof ContentText || o instanceof ContentQuote)
                    sb.append(o.getCopyText());
            }
            return Utils.trim(sb.toString());
        }

        public String getContent() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                ContentAbs o = list.get(i);
                if (o instanceof ContentText)
                    sb.append(o.getContent());
            }
            return sb.toString();
        }
    }

    public String getAuthor() {
        return mAuthor;
    }

    public boolean setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;

        return !HiSettingsHelper.getInstance().isUserBlack(mAuthor);
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String mUid) {
        this.mUid = mUid;
    }

    public String getPostId() {
        return mPostId;
    }

    public void setPostId(String mPostId) {
        this.mPostId = mPostId;
    }

    public String getTimePost() {
        return mTimePost;
    }

    public void setTimePost(String mTimePost) {
        this.mTimePost = mTimePost.substring(4);
    }

    public int getFloor() {
        return mFloor;
    }

    public void setFloor(int floor) {
        mFloor = floor;
    }

    public String getPostStatus() {
        return mPostStatus;
    }

    public void setPostStatus(String mPostStatus) {
        this.mPostStatus = mPostStatus;
    }

    public Contents getContents() {
        return mContents;
    }

    public void setContents(Contents contents) {
        this.mContents = contents;
    }

    public Collection<ContentImg> getImages() {
        return mImages;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        if (avatarUrl.contains("noavatar")) {
            this.mAvatarUrl = "";
        } else {
            this.mAvatarUrl = avatarUrl;
        }
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public boolean isWarned() {
        return mWarned;
    }

    public void setWarned(boolean warned) {
        mWarned = warned;
    }

    public boolean isSelectMode() {
        return mSelectMode;
    }

    public void setSelectMode(boolean selectMode) {
        mSelectMode = selectMode;
    }

    public boolean isHighlightMode() {
        return mHighlightMode;
    }

    public void setHighlightMode(boolean highlightMode) {
        mHighlightMode = highlightMode;
    }

    private String unEscapeHtml(String str) {
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("&quot;", "\"");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");

        return str;
    }
}
