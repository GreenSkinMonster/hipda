package net.jejer.hipda.ui;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import net.jejer.hipda.R;
import net.jejer.hipda.async.UploadImgAsyncTask;

public class UploadImgButton extends Button implements UploadImgAsyncTask.UploadImgListener {
    public final static int STAGE_UPLOADING = -1;

    private String mId;
    private String mName;
    private Context mCtx;
    private PostFragment mPostFragment;

    public UploadImgButton(Context context, PostFragment postFragment) {
        super(context);
        mCtx = context;
        mPostFragment = postFragment;
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
            this.setText("正在上传 " + percentage + "%");
            this.setTextColor(mCtx.getResources().getColor(R.color.orange));
        }
    }

    @Override
    public void complete(boolean result, String id) {
        this.setEnabled(result);
        if (result && !TextUtils.isEmpty(id) && TextUtils.isDigitsOnly(id)) {
            this.setEnabled(true);
            mId = id;
            this.setText("点击添加" + mName);
            this.setTextColor(mCtx.getResources().getColor(R.color.icon_blue));
            mPostFragment.appendImage(getImgId());
            Toast.makeText(mCtx, "图片已经添加至发表内容中", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mCtx, "图片上传失败或图片太大", Toast.LENGTH_LONG).show();
            setVisibility(GONE);
        }
    }

}
