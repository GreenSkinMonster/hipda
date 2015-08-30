package net.jejer.hipda.bean;


public class ThreadBean {

    private String mTitle;
    private String mTid;
    private boolean mIsStick;

    private String mAuthor;
    private String mAuthorId;
    private String mAvatarUrl;
    private String mLastPost;

    private String mCountCmts;
    private String mCountViews;

    private String mTimeCreate;
    private String mTimeUpdate;
    private Boolean mHaveAttach;
    private Boolean mHavePic;
    private boolean mNew;
    private String mType;
    private int mMaxPage;

    public ThreadBean() {
        mIsStick = false;
        mHaveAttach = false;
        mHavePic = false;
    }

    public String getTitle() {
        return mTitle;
    }


    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }


    public String getTid() {
        return mTid;
    }


    public void setTid(String mTid) {
        this.mTid = mTid;
    }


    public boolean getIsStick() {
        return mIsStick;
    }


    public void setIsStick(boolean mIsStick) {
        this.mIsStick = mIsStick;
    }


    public String getAuthor() {
        return mAuthor;
    }


    // return false if author is in blacklist
    public boolean setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;

        return !HiSettingsHelper.getInstance().isUserBlack(mAuthor);
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

    public Boolean getHaveAttach() {
        return mHaveAttach;
    }

    public void setHaveAttach(Boolean mHaveAttach) {
        this.mHaveAttach = mHaveAttach;
    }


    public Boolean getHavePic() {
        return mHavePic;
    }

    public void setHavePic(Boolean mHavePic) {
        this.mHavePic = mHavePic;
    }

    public boolean isNew() {
        return mNew;
    }

    public void setNew(boolean isNew) {
        this.mNew = isNew;
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
