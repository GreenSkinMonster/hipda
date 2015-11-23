package net.jejer.hipda.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;

import net.jejer.hipda.utils.CursorUtils;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.ImageFileInfo;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UploadImgAsyncTask extends AsyncTask<Uri, Integer, Void> {

    public final static int STAGE_UPLOADING = -1;
    public final static int MAX_QUALITY = 90;
    private static final int THUMB_SIZE = 128;

    private final static int MAX_PIXELS = 1200 * 1200; //file with this resolution, it's size should match to MAX_IMAGE_FILE_SIZE
    public final static int MAX_IMAGE_FILE_SIZE = 400 * 1024; // max file size 400K
    public final static int MAX_SPECIAL_FILE_SIZE = 4 * 1024 * 1024; // max upload file size : 8M

    private static final int UPLOAD_CONNECT_TIMEOUT = 15 * 1000;
    private static final int UPLOAD_READ_TIMEOUT = 5 * 60 * 1000;

    private UploadImgListener mListener;

    private String mUid;
    private String mHash;
    private Context mCtx;

    private Uri mCurrentUri;
    private String mMessage = "";
    private Bitmap mThumb;
    private int mTotal;
    private int mCurrent;
    private String mCurrentFileName = "";

    public UploadImgAsyncTask(Context ctx, UploadImgListener v, String uid, String hash) {
        mCtx = ctx;
        mListener = v;
        mUid = uid;
        mHash = hash;
    }

    public interface UploadImgListener {
        void updateProgress(Uri uri, int total, int current, String currentFileName, int percentage);

        void itemComplete(Uri uri, int total, int current, String currentFileName, String message, String imgId, Bitmap thumbtail);

        void complete();
    }

    @Override
    protected Void doInBackground(Uri... uris) {

        Map<String, String> post_param = new HashMap<>();

        post_param.put("uid", mUid);
        post_param.put("hash", mHash);

        mTotal = uris.length;

        int i = 0;
        for (Uri uri : uris) {
            mCurrent = i++;
            String imgId = doUploadFile(HiUtils.UploadImgUrl, post_param, uri);
            mListener.itemComplete(uri, mTotal, mCurrent, mCurrentFileName, mMessage, imgId, mThumb);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (mListener != null) {
            mListener.updateProgress(mCurrentUri, mTotal, mCurrent, mCurrentFileName, progress[0]);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mListener != null) {
            mListener.complete();
        }
    }

    private static String getBoundry() {
        StringBuilder sb = new StringBuilder();
        for (int t = 1; t < 12; t++) {
            long time = System.currentTimeMillis() + t;
            if (time % 3 == 0) {
                sb.append((char) time % 9);
            } else if (time % 3 == 1) {
                sb.append((char) (65 + time % 26));
            } else {
                sb.append((char) (97 + time % 26));
            }
        }
        return sb.toString();
    }

    private String getBoundaryMessage(String boundary, Map<String, String> params, String fileField, String fileName, String fileType) {
        StringBuilder res = new StringBuilder("--").append(boundary).append("\r\n");

        for (String key : params.keySet()) {
            String value = params.get(key);
            res.append("Content-Disposition: form-data; name=\"")
                    .append(key).append("\"\r\n").append("\r\n")
                    .append(value).append("\r\n").append("--")
                    .append(boundary).append("\r\n");
        }
        res.append("Content-Disposition: form-data; name=\"").append(fileField)
                .append("\"; filename=\"").append(fileName)
                .append("\"\r\n").append("Content-Type: ")
                .append(fileType).append("\r\n\r\n");

        return res.toString();
    }

    public String doUploadFile(String urlStr, Map<String, String> param, Uri uri) {

        mCurrentUri = uri;
        mThumb = null;
        mMessage = "";
        mCurrentFileName = "";

        // update progress for start compress
        publishProgress(STAGE_UPLOADING);

        ImageFileInfo imageFileInfo = CursorUtils.getImageFileInfo(mCtx, uri);
        mCurrentFileName = imageFileInfo.getFileName();

        ByteArrayOutputStream baos = compressImage(uri, imageFileInfo);
        if (baos == null) {
            return null;
        }

        String fileType = imageFileInfo.getMime();
        String imageParamName = "Filedata";

        // update progress for start upload
        publishProgress(0);

        String BOUNDARYSTR = getBoundry();

        byte[] barry = null;
        int contentLength = 0;
        String sendStr = "";
        try {
            barry = ("--" + BOUNDARYSTR + "--\r\n").getBytes("UTF-8");
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd_HHmm", Locale.US);
            String fileName = "Hi_" + formatter.format(new Date()) + "." + Utils.getImageFileSuffix(imageFileInfo.getMime());
            sendStr = getBoundaryMessage(BOUNDARYSTR, param, imageParamName, fileName, fileType);
            contentLength = sendStr.getBytes("UTF-8").length + baos.size() + 2 * barry.length;
        } catch (UnsupportedEncodingException ignored) {

        }
        String lenstr = Integer.toString(contentLength);

        String imgId = "";
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestProperty("User-Agent", HiUtils.getUserAgent());

            urlConnection.setConnectTimeout(UPLOAD_CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(UPLOAD_READ_TIMEOUT);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-type", "multipart/form-data;boundary=" + BOUNDARYSTR);
            urlConnection.setRequestProperty("Content-Length", lenstr);
            urlConnection.setFixedLengthStreamingMode(contentLength);
            urlConnection.connect();

            out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(sendStr.getBytes("UTF-8"));

            int bytesLeft;
            int transferred = 0;
            int postSize;
            int maxPostSize = 4096;

            bytesLeft = baos.size();
            postSize = Math.min(bytesLeft, maxPostSize);
            final Thread thread = Thread.currentThread();
            long mark = SystemClock.uptimeMillis();
            while (bytesLeft > 0) {
                if (thread.isInterrupted()) {
                    throw new InterruptedIOException();
                }
                out.write(baos.toByteArray(), transferred, postSize);
                transferred += postSize;
                bytesLeft -= postSize;
                postSize = Math.min(bytesLeft, maxPostSize);
                if (SystemClock.uptimeMillis() - mark > 250) {
                    out.flush();
                    mark = SystemClock.uptimeMillis();
                }
                publishProgress((int) ((transferred * 100) / baos.size()));
            }

            //yes, write twice
            out.write(barry);
            out.write(barry);
            out.flush();
            int status = urlConnection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                mMessage = "上传错误代码 : " + status;
                return null;
            }
            Logger.v("uploading image, response : " + urlConnection.getResponseCode() + ", " + urlConnection.getResponseMessage());
            InputStream in = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                imgId += inputLine;
            }

        } catch (Exception e) {
            Logger.e("Error uploading image", e);
            mMessage = "上传发生网络错误 : " + e.getMessage();
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {

                }
            }
            try {
                baos.close();
            } catch (IOException ignored) {

            }
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        // DISCUZUPLOAD|0|1721652|1
        if (!imgId.startsWith("DISCUZUPLOAD")) {
            mMessage = "错误的图片ID : " + imgId;
            return null;
        } else {
            String[] s = imgId.split("\\|");
            if (s.length < 3 || s[2].equals("0")) {
                mMessage = "错误的图片ID : " + imgId;
                return null;
            } else {
                imgId = s[2];
            }
        }

        return imgId;
    }

    private ByteArrayOutputStream compressImage(Uri uri, ImageFileInfo imageFileInfo) {

        if (imageFileInfo.isGif()
                && imageFileInfo.getFileSize() > MAX_SPECIAL_FILE_SIZE) {
            mMessage = "GIF图片大小不能超过" + Utils.toSizeText(MAX_SPECIAL_FILE_SIZE);
            return null;
        }

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(mCtx.getContentResolver(), uri);
        } catch (Exception e) {
            Logger.v("Exception", e);
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
        if (imageFileInfo.isGif() && fileSize <= MAX_SPECIAL_FILE_SIZE)
            return true;

        //very long or wide image
        if (w > 0 && h > 0 && fileSize <= MAX_SPECIAL_FILE_SIZE) {
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
