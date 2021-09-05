package net.jejer.hipda.bean;


public class ThreadBean {

    private String mTitle;
    private String mTitleColor;
    private String mTid;
    private boolean mSticky;

    private String mAuthor;
    private String mAuthorId;
    private String mAvatarUrl;
    private String mLastPost;

    private String mCountCmts;
    private String mCountViews;

    private String mTimeCreate;
    private String mTimeUpdate;
    private boolean mWithPic;
    private boolean mNew;
    private boolean mPoll;
    private String mType;
    private int mMaxPage;

    public ThreadBean() {
    }

    public String getTitle() {
        return mTitle;
    }


    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getTitleColor() {
        return mTitleColor;
    }

    public void setTitleColor(String mTitleColor) {
        this.mTitleColor = mTitleColor;
    }

    public String getTid() {
        return mTid;
    }

    public void setTid(String mTid) {
        this.mTid = mTid;
    }

    public boolean isSticky() {
        return mSticky;
    }

    public void setSticky(boolean sticky) {
        this.mSticky = sticky;
    }

    public String getAuthor() {
        return mAuthor;
    }

    // return false if author is in blacklist
    public boolean setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;

        return !HiSettingsHelper.getInstance().isInBlacklist(mAuthor);
    }

    public String getAuthorId() {
        return mAuthorId;
    }

    public void setAuthorId(String mAuthorId) {
        this.mAuthorId = mAuthorId;
    }

    public String getLastPost() {
        return mLastPost;
    }

    public void setLastPost(String mLastPost) {
        this.mLastPost = mLastPost;
    }

    public String getCountCmts() {
        return mCountCmts;
    }

    public void setCountCmts(String mCountCmts) {
        this.mCountCmts = mCountCmts;
    }

    public String getCountViews() {
        return mCountViews;
    }

    public void setCountViews(String mCountViews) {
        this.mCountViews = mCountViews;
    }

    public String getTimeUpdate() {
        return mTimeUpdate;
    }

    public void setTimeUpdate(String timeUpdate) {
        mTimeUpdate = timeUpdate;
    }

    public String getTimeCreate() {
        return mTimeCreate;
    }

    public void setTimeCreate(String mTimeCreate) {
        this.mTimeCreate = mTimeCreate;
    }

    public boolean isWithPic() {
        return mWithPic;
    }

    public void setWithPic(boolean mHavePic) {
        this.mWithPic = mHavePic;
    }

    public boolean isNew() {
        return mNew;
    }

    public void setNew(boolean isNew) {
        this.mNew = isNew;
    }

    public boolean isPoll() {
        return mPoll;
    }

    public void setPoll(boolean poll) {
        mPoll = poll;
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

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public int getMaxPage() {
        return mMaxPage;
    }

    public void setMaxPage(int lastPage) {
        this.mMaxPage = lastPage;
    }
}
