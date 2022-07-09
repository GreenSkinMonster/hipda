package net.jejer.hipda.async;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.CursorUtils;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.ImageFileInfo;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadImgHelper {

    private final static int MAX_QUALITY = 90;
    private static final int THUMB_SIZE = 256;

    private int mMaxImageFileSize = 2048 * 1024;
    private int mMaxPixels = 2560 * 2560;

    private UploadImgListener mListener;

    private String mUid;
    private String mHash;
    private Uri[] mUris;
    private boolean mOriginal;

    private String mMessage = "";
    private String mDetail = "";
    private Bitmap mThumb;
    private int mTotal;
    private int mCurrent;

    public UploadImgHelper(UploadImgListener v, String uid, String hash, Uri[] uris, boolean original) {
        mListener = v;
        mUid = uid;
        mHash = hash;
        mUris = uris;
        mOriginal = original;

        int maxUploadSize = HiSettingsHelper.getInstance().getMaxUploadFileSize();
        if (maxUploadSize > 0 && mMaxImageFileSize > maxUploadSize) {
            mMaxPixels = (int) (0.6 * mMaxPixels);
            mMaxImageFileSize = maxUploadSize;
        }
    }

    public interface UploadImgListener {
        void updateProgress(int total, int current, int percentage);

        void itemComplete(Uri uri, int total, int current, String message, String detail, String imgId, Bitmap thumbtail);
    }

    public void upload() {
        Map<String, String> postParam = new HashMap<>();

        postParam.put("uid", mUid);
        postParam.put("hash", mHash);

        mTotal = mUris.length;

        int i = 0;
        for (Uri uri : mUris) {
            mCurrent = i++;
            mListener.updateProgress(mTotal, mCurrent, -1);
            String imgId = uploadImage(postParam, uri);
            mListener.itemComplete(uri, mTotal, mCurrent, mMessage, mDetail, imgId, mThumb);
        }
    }

    private String uploadImage(Map<String, String> param, Uri uri) {
        mThumb = null;
        mMessage = "";
        File tmpFile = new File(Utils.getUploadDir() + File.separator + System.currentTimeMillis());
        try {
            Utils.copy(uri, tmpFile);
        } catch (Exception e) {
            mMessage = "无法读取选择的图片文件";
            mDetail = "\n" + uri.toString()
                    + "\n" + Utils.getStackTrace(e);
            Logger.e(e);
            return null;
        }

        ImageFileInfo imageFileInfo = CursorUtils.getImageFileInfo(tmpFile);
        if (imageFileInfo == null) {
            mMessage = "无法解析图片信息";
            mDetail = "\n" + uri.toString();
            return null;
        }

        if (imageFileInfo.isGif()
                && imageFileInfo.getFileSize() > HiSettingsHelper.getInstance().getMaxUploadFileSize()) {
            mMessage = "GIF图片大小不能超过" + Utils.toSizeText(HiSettingsHelper.getInstance().getMaxUploadFileSize());
            return null;
        }
        ByteArrayOutputStream baos;
        try {
            baos = getImageStream(imageFileInfo);
        } catch (Exception e) {
            mMessage = "处理图片发生错误";
            mDetail = "\n" + uri.toString()
                    + "\n" + Utils.getStackTrace(e);
            return null;
        }
        if (baos == null) {
            if (TextUtils.isEmpty(mMessage)) {
                mMessage = "处理图片发生错误";
                mDetail = "\n" + uri.toString();
            }
            return null;
        }


        String imgId = null;
        try {
            imgId = postImage(param, imageFileInfo, baos);
        } catch (Exception e) {
            Logger.e(e);
            mMessage = OkHttpHelper.getErrorMessage(e).getMessage();
            mDetail = "原图限制：" + Utils.toSizeText(HiSettingsHelper.getInstance().getMaxUploadFileSize())
                    + "\n压缩目标：" + Utils.toSizeText(mMaxImageFileSize)
                    + "\n实际大小：" + Utils.toSizeText(baos.size())
                    + "\n" + e.getMessage();
        } finally {
            try {
                baos.close();
                if (tmpFile.exists())
                    tmpFile.delete();
            } catch (IOException ignored) {
            }
        }
        return imgId;
    }

    private String postImage(Map<String, String> param, ImageFileInfo imageFileInfo, ByteArrayOutputStream baos) throws IOException {
        String imgId = null;
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (String key : param.keySet()) {
            builder.addFormDataPart(key, param.get(key));
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmm", Locale.US);
        String fileName = "Hi_" + formatter.format(new Date()) + "." + Utils.getImageFileSuffix(imageFileInfo.getMime());
        RequestBody requestBody = RequestBody.create(MediaType.parse(imageFileInfo.getMime()), baos.toByteArray());
        builder.addFormDataPart("Filedata", fileName, requestBody);

        Request request = new Request.Builder()
                .url(HiUtils.UploadImgUrl)
                .post(builder.build())
                .build();

        Response response = OkHttpHelper.getInstance().getClient().newCall(request).execute();
        if (!response.isSuccessful())
            throw new IOException(OkHttpHelper.ERROR_CODE_PREFIX + response.networkResponse().code());

        String responseText = response.body().string();
        // DISCUZUPLOAD|0|1721652|1
        if (responseText.contains("DISCUZUPLOAD")) {
            String[] s = responseText.split("\\|");
            if (s.length < 3 || s[2].equals("0")) {
                mMessage = "无效上传图片ID";
                mDetail = "原图限制：" + Utils.toSizeText(HiSettingsHelper.getInstance().getMaxUploadFileSize())
                        + "\n压缩目标：" + Utils.toSizeText(mMaxImageFileSize)
                        + "\n实际大小：" + Utils.toSizeText(baos.size())
                        + "\n" + responseText;
            } else {
                imgId = s[2];
            }
        } else {
            mMessage = "无法获取图片ID";
            mDetail = "原图限制：" + Utils.toSizeText(HiSettingsHelper.getInstance().getMaxUploadFileSize())
                    + "\n压缩目标：" + Utils.toSizeText(mMaxImageFileSize)
                    + "\n实际大小：" + Utils.toSizeText(baos.size())
                    + "\n" + responseText;
        }
        return imgId;
    }

    private ByteArrayOutputStream getImageStream(ImageFileInfo imageFileInfo) throws Exception {
        Bitmap bitmap = BitmapFactory.decodeFile(imageFileInfo.getFilePath());

        //gif or very long/wide image or small image or filePath is null
        if (isDirectUploadable(imageFileInfo)) {
            mThumb = ThumbnailUtils.extractThumbnail(bitmap, THUMB_SIZE, THUMB_SIZE);
            bitmap.recycle();
            return readFileToStream(imageFileInfo.getFilePath());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, MAX_QUALITY, baos);
        bitmap.recycle();
        bitmap = null;

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(isBm, null, opts);

        int width = opts.outWidth;
        int height = opts.outHeight;

        //inSampleSize is needed to avoid OOM
        int be = width * height / mMaxPixels;
        if (be <= 0)
            be = 1; //be=1表示不缩放
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = false;
        newOpts.inSampleSize = be;

        isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap newbitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

        width = newbitmap.getWidth();
        height = newbitmap.getHeight();

        //scale bitmap so later compress could run less times, once is the best result
        //rotate if needed
        if (width * height > mMaxPixels
                || imageFileInfo.getOrientation() > 0) {

            float scale = 1.0f;
            if (width * height > mMaxPixels) {
                scale = (float) Math.sqrt(mMaxPixels * 1.0 / (width * height));
            }

            Matrix matrix = new Matrix();
            if (imageFileInfo.getOrientation() > 0)
                matrix.postRotate(imageFileInfo.getOrientation());
            if (scale < 1)
                matrix.postScale(scale, scale);

            Bitmap scaledBitmap = Bitmap.createBitmap(newbitmap, 0, 0, newbitmap.getWidth(),
                    newbitmap.getHeight(), matrix, true);

            newbitmap.recycle();
            newbitmap = scaledBitmap;
        }

        int quality = MAX_QUALITY;
        baos.reset();
        newbitmap.compress(CompressFormat.JPEG, quality, baos);
        while (baos.size() > mMaxImageFileSize) {
            quality -= 10;
            if (quality <= 50) {
                mMessage = "无法压缩图片至指定大小 " + Utils.toSizeText(mMaxImageFileSize);
                return null;
            }
            baos.reset();
            newbitmap.compress(CompressFormat.JPEG, quality, baos);
        }

        mThumb = ThumbnailUtils.extractThumbnail(newbitmap, THUMB_SIZE, THUMB_SIZE);
        newbitmap.recycle();
        newbitmap = null;

        return baos;
    }

    private boolean isDirectUploadable(ImageFileInfo imageFileInfo) {
        if (mOriginal)
            return true;

        long fileSize = imageFileInfo.getFileSize();
        int w = imageFileInfo.getWidth();
        int h = imageFileInfo.getHeight();

        if (TextUtils.isEmpty(imageFileInfo.getFilePath()))
            return false;

        if (imageFileInfo.getOrientation() > 0)
            return false;

        //gif image
        if (imageFileInfo.isGif() && fileSize <= HiSettingsHelper.getInstance().getMaxUploadFileSize())
            return true;

        //very long or wide image
        if (w > 0 && h > 0 && fileSize <= HiSettingsHelper.getInstance().getMaxUploadFileSize()) {
            if (Math.max(w, h) * 1.0 / Math.min(w, h) >= 3)
                return true;
        }

        //normal image
        return fileSize <= mMaxImageFileSize && w * h <= mMaxPixels;
    }

    private static ByteArrayOutputStream readFileToStream(String file) {
        FileInputStream fileInputStream = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            fileInputStream = new FileInputStream(file);
            int readedBytes;
            byte[] buf = new byte[1024];
            while ((readedBytes = fileInputStream.read(buf)) > 0) {
                bos.write(buf, 0, readedBytes);
            }
            return bos;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (Exception ignored) {

            }
        }
    }

}
