package net.jejer.hipda.bean;

import net.jejer.hipda.utils.HiUtils;

public class ContentImg extends ContentAbs {
	private String mUrl;
	private boolean mInternal;

	public ContentImg(String url, boolean isInternal) {
		mInternal = isInternal;
		mUrl = url;
	}

	public void setInternalUrl(String url) {
		mUrl = url;
		mInternal = true;
	}
	public void setExternalUrl(String url) {
		mUrl = url;
		mInternal = false;
	}

	@Override
	public String getContent() {
		// TODO Auto-generated method stub
		if (mInternal) {
			return HiUtils.getFullUrl(mUrl);
		} else {
			return mUrl;
		}
	}

	@Override
	public String getCopyText() {
		return "[图片:"+mUrl+"]";
	}

}
