package net.jejer.hipda.ui;

import net.jejer.hipda.R;
import net.jejer.hipda.async.UploadImgAsyncTask;
import android.content.Context;
import android.widget.Button;

public class UploadImgButton extends Button implements UploadImgAsyncTask.UploadImgListener {
	public final static int STAGE_UPLOADING = -1;

	private String mId;
	private String mName;
	private Context mCtx;

	public UploadImgButton(Context context) {
		super(context);
		mCtx = context;
	}

	public void setImgName(String name) {
		if (name != null) {
			mName = name;
		} else {
			mName = "HiPDA_UPLOAD.jpg";
		}
		this.setText(mName);
	}

	public String getImgId() {
		return mId;
	}

	@Override
	public void updateProgress(int percentage) {
		if (percentage == STAGE_UPLOADING) {
			this.setText("正在压缩图片(~300K)");
		} else {
			this.setText("正在上传 "+percentage+"%");
			this.setTextColor(mCtx.getResources().getColor(R.color.orange));
		}
	}

	@Override
	public void complete(boolean result, String id) {
		this.setEnabled(result);
		if (result == true) {
			this.setEnabled(true);
			mId = id;
			this.setText("点击添加"+mName);
			this.setTextColor(mCtx.getResources().getColor(R.color.red));
		} else {
			this.setText("上传失败或图片太大");
			this.setTextColor(mCtx.getResources().getColor(R.color.red));
		}
	}

}
