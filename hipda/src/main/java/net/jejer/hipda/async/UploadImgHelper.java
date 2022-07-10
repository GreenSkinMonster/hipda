package net.jejer.hipda.async;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.TextUtils;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.ui.HiApplication;
import net.jejer.hipda.utils.CursorUtils;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.ImageFileInfo;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadImgHelper {

    private final static int MAX_QUALITY = 80;
    private final static int MAX_DIMENSION = 2560;
    private static final int THUMB_SIZE = 256;

    private int mMaxUploadSize = 2048 * 1024;

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

    private File mTempFile;
    private File mCompressedFile;

    public UploadImgHelper(UploadImgListener v, String uid, String hash, Uri[] uris, boolean original) {
        mListener = v;
        mUid = uid;
        mHash = hash;
        mUris = uris;
        mOriginal = original;

        int maxUploadSize = HiSettingsHelper.getInstance().getMaxUploadFileSize();
        if (maxUploadSize > 0) {
            mMaxUploadSize = maxUploadSize;
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

    private static String getRandonTempFilename(Uri uri) {
        try {
            return Utils.md5(uri.toString() + System.currentTimeMillis());
        } catch (Exception e) {
            return System.currentTimeMillis() + "";
        }
    }

    private String uploadImage(Map<String, String> param, Uri uri) {
        mThumb = null;
        mMessage = "";
        mTempFile = new File(Utils.getUploadDir() + File.separator + getRandonTempFilename(uri));
        try {
            Utils.copy(uri, mTempFile);
        } catch (Exception e) {
            mMessage = "无法读取选择的图片文件";
            mDetail = "\n" + uri.toString()
                    + "\n" + Utils.getStackTrace(e);
            Logger.e(e);
            return null;
        }

        ImageFileInfo imageFileInfo = CursorUtils.getImageFileInfo(mTempFile);
        if (imageFileInfo == null) {
            mMessage = "无法解析图片信息";
            mDetail = "\n" + uri.toString();
            return null;
        }

        if (imageFileInfo.isGif()
                && imageFileInfo.getFileSize() > mMaxUploadSize) {
            mMessage = "GIF图片大小不能超过" + Utils.toSizeText(mMaxUploadSize);
            return null;
        }

        ByteArrayOutputStream baos;
        try {
            Bitmap bitmap;
            if (isDirectUploadable(imageFileInfo)) {
                baos = readFileToStream(imageFileInfo.getFilePath());
                bitmap = BitmapFactory.decodeFile(imageFileInfo.getFilePath());
            } else {
                mCompressedFile = compressImageFile(imageFileInfo);
                if (mCompressedFile == null) {
                    mMessage = "无法压缩图片至指定大小 " + Utils.toSizeText(mMaxUploadSize);
                    mDetail = "\n" + uri.toString()
                            + "\n文件类型 : " + imageFileInfo.getMime()
                            + "\n原始大小 : " + Utils.toSizeText(imageFileInfo.getFileSize());
                    return null;
                }
                baos = readFileToStream(mCompressedFile.getAbsolutePath());
                bitmap = BitmapFactory.decodeFile(mCompressedFile.getAbsolutePath());
            }
            mThumb = ThumbnailUtils.extractThumbnail(bitmap, THUMB_SIZE, THUMB_SIZE);
            bitmap.recycle();
        } catch (Exception e) {
            mMessage = "处理图片发生错误";
            mDetail = "\n" + uri.toString()
                    + "\n" + Utils.getStackTrace(e);
            return null;
        }

        if (baos == null) {
            if (TextUtils.isEmpty(mMessage)) {
                mMessage = "处理图片发生错误2";
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
            mDetail = "原图限制：" + Utils.toSizeText(mMaxUploadSize)
                    + "\n实际大小：" + Utils.toSizeText(baos.size())
                    + "\n" + e.getMessage();
        } finally {
            try {
                baos.close();
                if (mTempFile != null && mTempFile.exists())
                    mTempFile.delete();
                if (mCompressedFile != null && mCompressedFile.exists())
                    mCompressedFile.delete();
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmmss", Locale.US);
        String fileName = "Hi" + (mCompressedFile != null ? "_" : "-") + formatter.format(new Date()) + "." + Utils.getImageFileSuffix(imageFileInfo.getMime());
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
                mDetail = "原图限制：" + Utils.toSizeText(mMaxUploadSize)
                        + "\n实际大小：" + Utils.toSizeText(baos.size())
                        + "\n" + responseText;
            } else {
                imgId = s[2];
            }
        } else {
            mMessage = "无法获取图片ID";
            mDetail = "原图限制：" + Utils.toSizeText(mMaxUploadSize)
                    + "\n实际大小：" + Utils.toSizeText(baos.size())
                    + "\n" + responseText;
        }
        return imgId;
    }

    private File compressImageFile(ImageFileInfo imageFileInfo) throws Exception {
        int maxDimension = Math.max(imageFileInfo.getWidth(), imageFileInfo.getHeight());
        maxDimension = Math.min(maxDimension, MAX_DIMENSION);
        String compressedFilename = getRandonTempFilename(Uri.fromFile(mTempFile));
        for (int i = 0; i < 5; i++) {
            int dimension = (int) (maxDimension * (5 - i) * 0.1);
            File compressedFile = new Compressor(HiApplication.getAppContext())
                    .setMaxWidth(dimension)
                    .setMaxHeight(dimension)
                    .setQuality(MAX_QUALITY)
                    .setCompressFormat(CompressFormat.JPEG)
                    .setDestinationDirectoryPath(Utils.getUploadDir().getAbsolutePath())
                    .compressToFile(mTempFile, compressedFilename);
            if (compressedFile.length() <= mMaxUploadSize) {
                return compressedFile;
            }
        }
        return null;
    }

    private boolean isDirectUploadable(ImageFileInfo imageFileInfo) {
        int w = imageFileInfo.getWidth();
        int h = imageFileInfo.getHeight();

        //原图/gif/超长图
        return (mOriginal || imageFileInfo.isGif() || (Math.max(w, h) * 1.0 / Math.min(w, h) >= 3))
                && imageFileInfo.getOrientation() <= 0
                && isMimeSupported(imageFileInfo.getMime())
                && imageFileInfo.getFileSize() <= mMaxUploadSize;
    }

    private boolean isMimeSupported(String mime) {
        return !TextUtils.isEmpty(mime)
                && (mime.contains("jpg")
                || mime.contains("jpeg")
                || mime.contains("png")
                || mime.contains("gif")
                || mime.contains("bmp"));
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
