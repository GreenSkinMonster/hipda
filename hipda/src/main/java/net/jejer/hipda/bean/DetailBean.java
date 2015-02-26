package net.jejer.hipda.bean;

import java.util.ArrayList;

public class DetailBean {
	private String mAuthor;
	private String mUid;
	private String mAvatarUrl;
	private String mPostId;
	private String mTimePost;
	private String mFloor;
	private String mPostStatus;
	private Contents mContents;

	public DetailBean() {
		mContents = new Contents();
	}

	public class Contents {
		private ArrayList<ContentAbs> list;
		private int lastTextIdx;
		private Boolean newString;

		public Contents() {
			list = new ArrayList<ContentAbs>();
			lastTextIdx = -1;
			newString = true;
		}

		public void addText(String text) {
			if (newString) {
				list.add(new ContentText(unEscapeHtml(text)));
				lastTextIdx = list.size()-1;
				newString = false;
			} else {
				ContentText ct = (ContentText)list.get(lastTextIdx);
				ct.append(unEscapeHtml(text));
			}
		}

		public void addLink(String text, String url) {
			String link= "[<a href=\""+url+"\">"+text+"</a>]";
			if (newString) {
				list.add(new ContentText(link));
				lastTextIdx = list.size()-1;
				newString = false;
			} else {
				ContentText ct = (ContentText)list.get(lastTextIdx);
				ct.append(link);
			}
		}

		public void addImg(String url, Boolean isInternal) {
			list.add(new ContentImg(url, isInternal));
			newString = true;
		}

		public void addAttach(String url, String title) {
			list.add(new ContentAttach(url, unEscapeHtml(title)));
			newString = true;
		}

		public void addQuote(String text) {
			list.add(new ContentQuote(unEscapeHtml(text)));
			newString = true;
		}

		public void addGoToFloor(String text, int floor) {
			list.add(new ContentGoToFloor(text, floor));
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
				sb.append(o.getCopyText());
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

	public String getFloor() {
		return mFloor;
	}

	public void setFloor(String mFloor) {
		this.mFloor = mFloor;
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

	public String getAvatarUrl() {
		return mAvatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		if (avatarUrl.contains("noavatar")) {
			this.mAvatarUrl = "";
		} else {
			this.mAvatarUrl = avatarUrl.replaceAll("middle", "small");
		}
	}

	private String unEscapeHtml(String str) {
		str = str.replaceAll("&nbsp;"," ");
		str = str.replaceAll("&quot;","\"");
		str = str.replaceAll("&amp;","&");
		str = str.replaceAll("&lt;","<");
		str = str.replaceAll("&gt;",">");

		return str;
	}
}
