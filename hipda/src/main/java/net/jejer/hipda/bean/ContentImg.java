package net.jejer.hipda.bean;

import android.os.Parcel;
import android.os.Parcelable;

import net.jejer.hipda.utils.Utils;

public class ContentImg extends ContentAbs implements Parcelable {
    private String mUrl;
    private String mThumbUrl;
    private String mAuthor;
    private int mFloor;
    private int mIndexInPage;
    private long mFileSize;

    public ContentImg(String url, long fileSize, String thumbUrl) {
        mUrl = url;
        mFileSize = fileSize;
        mThumbUrl = thumbUrl;
    }

    protected ContentImg(Parcel in) {
        mUrl = in.readString();
        mThumbUrl = in.readString();
        mAuthor = in.readString();
        mFloor = in.readInt();
        mIndexInPage = in.readInt();
        mFileSize = in.readLong();
    }

    @Override
    public String getContent() {
        return mUrl;
    }

    @Override
    public String getCopyText() {
        return "[图片:" + mUrl + "]";
    }

    public int getFloor() {
        return mFloor;
    }

    public void setFloor(int floor) {
        mFloor = floor;
    }

    public int getIndexInPage() {
        return mIndexInPage;
    }

    public void setIndexInPage(int indexInPage) {
        mIndexInPage = indexInPage;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }


    public long getFileSize() {
        return mFileSize;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public static final Creator<ContentImg> CREATOR = new Creator<ContentImg>() {
        @Override
        public ContentImg createFromParcel(Parcel in) {
            return new ContentImg(in);
        }

        @Override
        public ContentImg[] newArray(int size) {
            return new ContentImg[size];
        }
    };

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(Utils.nullToText(mUrl));
        parcel.writeString(Utils.nullToText(mThumbUrl));
        parcel.writeString(Utils.nullToText(mAuthor));
        parcel.writeInt(mFloor);
        parcel.writeInt(mIndexInPage);
        parcel.writeLong(mFileSize);
    }
}
