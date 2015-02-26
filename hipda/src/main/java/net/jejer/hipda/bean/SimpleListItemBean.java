package net.jejer.hipda.bean;

public class SimpleListItemBean {
	private String mId;
	private String mPid;
	private String mTitle;
	private String mInfo;
	private String mTime;
	private String mAuthor;
	private String mAvatarUrl = "";;
	private boolean mNew = false;
	
	public String getId() {
		return mId;
	}
	public void setId(String mId) {
		this.mId = mId;
	}
	public String getPid() {
		return mPid;
	}
	public void setPid(String mPid) {
		this.mPid = mPid;
	}
	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	public String getInfo() {
		return mInfo;
	}
	public void setInfo(String mInfo) {
		this.mInfo = mInfo;
	}
	public String getTime() {
		return mTime;
	}
	public void setTime(String mTime) {
		this.mTime = mTime;
	}
	public boolean isNew() {
		return mNew;
	}
	public void setNew(boolean mNew) {
		this.mNew = mNew;
	}
	public String getAuthor() {
		return mAuthor;
	}
	public void setAuthor(String mAuthor) {
		this.mAuthor = mAuthor;
	}
	public String getAvatarUrl() {
		return mAvatarUrl;
	}
	public void setAvatarUrl(String mAvatarUrl) {
		if (mAvatarUrl.contains("noavatar")) {
			this.mAvatarUrl = "";
		} else {
			this.mAvatarUrl = mAvatarUrl.replaceAll("middle", "small");
		}
	}
	
	
}
