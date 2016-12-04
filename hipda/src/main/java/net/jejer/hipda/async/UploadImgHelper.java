package net.jejer.hipda.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
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

    public final static int MAX_QUALITY = 90;
    private static final int THUMB_SIZE = 192;

    private final static int MAX_PIXELS = 1200 * 1200; //file with this resolution, it's size should match to MAX_IMAGE_FILE_SIZE
    public final static int MAX_IMAGE_FILE_SIZE = 400 * 1024; // max file size 400K

    private UploadImgListener mListener;

    private String mUid;
    private String mHash;
    private Context mCtx;
    private Uri[] mUris;

    private Uri mCurrentUri;
    private String mMessage = "";
    private Bitmap mThumb;
    private int mTotal;
    private int mCurrent;
    private String mCurrentFileName = "";

    public UploadImgHelper(Context ctx, UploadImgListener v, String uid, String hash, Uri[] uris) {
        mCtx = ctx;
        mListener = v;
        mUid = uid;
        mHash = hash;
        mUris = uris;
    }

    public interface UploadImgListener {
        void updateProgress(int total, int current, int percentage);

        void itemComplete(Uri uri, int total, int current, String currentFileName, String message, String imgId, Bitmap thumbtail);
    }

    public void upload() {
        Map<String, String> post_param = new HashMap<>();

        post_param.put("uid", mUid);
        post_param.put("hash", mHash);

        mTotal = mUris.length;

        int i = 0;
        for (Uri uri : mUris) {
            mCurrent = i++;
            mListener.updateProgress(mTotal, mCurrent, -1);
            String imgId = uploadImage(HiUtils.UploadImgUrl, post_param, uri);
            mListener.itemComplete(uri, mTotal, mCurrent, mCurrentFileName, mMessage, imgId, mThumb);
        }
    }

    private String uploadImage(String urlStr, Map<String, String> param, Uri uri) {
        mCurrentUri = uri;
        mThumb = null;
        mMessage = "";
        mCurrentFileName = "";

        ImageFileInfo imageFileInfo = CursorUtils.getImageFileInfo(mCtx, uri);
        mCurrentFileName = imageFileInfo.getFileName();

        ByteArrayOutputStream baos = compressImage(uri, imageFileInfo);
        if (baos == null) {
            mMessage = "处理图片发生错误";
            return null;
        }

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
                .url(urlStr)
                .post(builder.build())
                .build();

        String imgId = null;
        try {
            Response response = OkHttpHelper.getInstance().getClient().newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException(OkHttpHelper.ERROR_CODE_PREFIX + response.networkResponse().code());

            String responseText = response.body().string();
            // DISCUZUPLOAD|0|1721652|1
            if (responseText.contains("DISCUZUPLOAD")) {
                String[] s = responseText.split("\\|");
                if (s.length < 3 || s[2].equals("0")) {
                    mMessage = "无法获取图片ID";
                } else {
                    imgId = s[2];
                }
            } else {
                mMessage = "无法获取图片ID";
            }
        } catch (Exception e) {
            Logger.e(e);
            mMessage = OkHttpHelper.getErrorMessage(e, false).getMessage();
        } finally {
            try {
                baos.close();
            } catch (IOException ignored) {
            }
        }
        return imgId;
    }

    private ByteArrayOutputStream compressImage(Uri uri, ImageFileInfo imageFileInfo) {
        if (imageFileInfo.isGif()
                && imageFileInfo.getFileSize() > HiSettingsHelper.getInstance().getMaxUploadFileSize()) {
            mMessage = "GIF图片大小不能超过" + Utils.toSizeText(HiSettingsHelper.getInstance().getMaxUploadFileSize());
            return null;
        }

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mCtx.getContentResolver(), uri);
        } catch (Exception e) {
            mMessage = "无法获取图片 : " + e.getMessage();
            return null;
        }

        //gif or very long image or small images etc
        if (isDirectUploadable(imageFileInfo)) {
            mThumb = ThumbnailUtils.extractThumbnail(bitmap, THUMB_SIZE, THUMB_SIZE);
            bitmap.recycle();
            return readFileToStream(imageFileInfo.getFilePath());
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, MAX_QUALITY, baos);

        if (baos.size() <= MAX_IMAGE_FILE_SIZE) {
            mThumb = ThumbnailUtils.extractThumbnail(bitmap, THUMB_SIZE, THUMB_SIZE);
            bitmap.recycle();
            bitmap = null;
            return baos;
        }
        bitmap.recycle();
        bitmap = null;

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(isBm, null, opts);

        int width = opts.outWidth;
        int height = opts.outHeight;

        //inSampleSize is needed to avoid OOM
        int be = (int) (Math.max(width, height) * 1.0 / 1500);
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
        if ((baos.size() > MAX_IMAGE_FILE_SIZE
                && width * height > MAX_PIXELS)
                || imageFileInfo.getOrientation() > 0) {

            float scale = 1.0f;
            if (width * height > MAX_PIXELS) {
                scale = (float) Math.sqrt(MAX_PIXELS * 1.0 / (width * height));
            }

            Matrix matrix = new Matrix();
            if (imageFileInfo.getOrientation() > 0)
                matrix.postRotate(imageFileInfo.getOrientation());
            matrix.postScale(scale, scale);

            Bitmap scaledBitmap = Bitmap.createBitmap(newbitmap, 0, 0, newbitmap.getWidth(),
                    newbitmap.getHeight(), matrix, true);

            newbitmap.recycle();
            newbitmap = scaledBitmap;
        }

        int quality = MAX_QUALITY;
        baos.reset();
        newbitmap.compress(CompressFormat.JPEG, quality, baos);
        while (baos.size() > MAX_IMAGE_FILE_SIZE) {
            quality -= 10;
            if (quality <= 0) {
                mMessage = "无法压缩图片至指定大小 " + Utils.toSizeText(MAX_IMAGE_FILE_SIZE);
                return null;
            }
            baos.reset();
            newbitmap.compress(CompressFormat.JPEG, quality, baos);
        }

        mThumb = ThumbnailUtils.extractThumbnail(newbitmap, THUMB_SIZE, THUMB_SIZE);
        newbitmap.recycle();
        newbitmap = null;

        System.gc();
        return baos;
    }

    private boolean isDirectUploadable(ImageFileInfo imageFileInfo) {
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
        return fileSize <= MAX_IMAGE_FILE_SIZE;
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
