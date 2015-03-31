package net.jejer.hipda.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.utils.HiUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class UploadImgAsyncTask extends AsyncTask<Bitmap, Integer, Boolean> {
    private final String LOG_TAG = getClass().getSimpleName();

    private static final int UPLOAD_CONNECT_TIMEOUT = 15 * 1000;
    private static final int UPLOAD_READ_TIMEOUT = 5 * 60 * 1000;

    private UploadImgListener mListener;
    private boolean mResult = true;
    private String mUid;
    private String mHash;
    private String mPicId = "";

    public UploadImgAsyncTask(Context ctx, UploadImgListener v, String uid, String hash) {
        //mCtx = ctx;
        mListener = v;
        mUid = uid;
        mHash = hash;
    }

    public interface UploadImgListener {
        void updateProgress(int percentage);

        void complete(boolean result, String id);
    }

    @Override
    protected Boolean doInBackground(Bitmap... arg) {

        Map<String, String> post_param = new HashMap<String, String>();

        post_param.put("uid", mUid);
        post_param.put("hash", mHash);

        Bitmap bitmap = arg[0];
        String fileType = "image/jpeg";

        doUploadFile(HiUtils.UploadImgUrl, post_param, bitmap, "Filedata", fileType);
        // DISCUZUPLOAD|0|1721652|1
        if (!mPicId.startsWith("DISCUZUPLOAD")) {
            mResult = false;
        } else {
            String[] s = mPicId.split("\\|");
            if (s.length < 3 || s[2].equals("0")) {
                mResult = false;
            } else {
                mPicId = s[2];
            }
        }
        return mResult;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (mListener != null) {
            mListener.updateProgress(progress[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.v("IMG_UPLOAD:", mPicId);
        if (mListener != null) {
            mListener.complete(mResult, mPicId);
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

    public boolean doUploadFile(String urlStr, Map<String, String> param, Bitmap bitmap, String imageParamName, String fileType) {
        String BOUNDARYSTR = getBoundry();

        // update progress for start compress
        publishProgress(-1);

        if (bitmap == null) {
            return false;
        }


        ByteArrayOutputStream baos = compressImage(bitmap);
        if (baos == null) {
            return false;
        }

        // update progress for start upload
        publishProgress(0);

        byte[] barry = null;
        int contentLength = 0;
        String sendStr = "";
        try {
            barry = ("--" + BOUNDARYSTR + "--\r\n").getBytes("UTF-8");
            sendStr = getBoundaryMessage(BOUNDARYSTR, param, imageParamName, "HiPDA_UPLOAD.jpg", fileType);
            contentLength = sendStr.getBytes("UTF-8").length + baos.size() + 2 * barry.length;
        } catch (UnsupportedEncodingException ignored) {

        }
        String lenstr = Integer.toString(contentLength);

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(urlStr);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestProperty("User-Agent", HiUtils.UserAgent);
            urlConnection.setRequestProperty("Cookie", "cdb_auth=" + HiSettingsHelper.getInstance().getCookieAuth());

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
            int maxPostSize = 1024;

            bytesLeft = baos.size();
            postSize = Math.min(bytesLeft, maxPostSize);
            final Thread thread = Thread.currentThread();
            while (bytesLeft > 0) {

                if (thread.isInterrupted()) {
                    throw new InterruptedIOException();
                }
                out.write(baos.toByteArray(), transferred, postSize);
                transferred += postSize;
                bytesLeft -= postSize;
                postSize = Math.min(bytesLeft, maxPostSize);
                if (transferred % 50 == 0)
                    out.flush();
                publishProgress((int) ((transferred * 100) / baos.size()));
            }

            //yes, write twice
            out.write(barry);
            out.write(barry);
            out.flush();
            int status = urlConnection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                mResult = false;
            }
            Log.v(LOG_TAG, "uploading image, response : " + urlConnection.getResponseCode() + ", " + urlConnection.getResponseMessage());
            InputStream in = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                mPicId += inputLine;
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error uploading image : " + e.getMessage());
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

        return true;
    }

    private ByteArrayOutputStream compressImage(Bitmap bitmap) {

        int maxQuality = 90;
        int maxImageSize = 300 * 1024; // max file size 300K

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, maxQuality, baos);

        int origionalSize = baos.size();
        if (origionalSize < maxImageSize * 0.8f)
            return baos;

        //Check and avoid BitmapFactory.decodeStream FC
        if (baos.size() / 1024 > 1024) {
            baos.reset();
            bitmap.compress(CompressFormat.JPEG, 50, baos);
        }

        //ignore ide suggestion, and do not change following 5 lines, unless you know exactly what to do
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap newbitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        float hh = 720f;
        float ww = 720f;

        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;

        isBm = new ByteArrayInputStream(baos.toByteArray());
        newbitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

        // HiPDA have 300KB limitation
        int quality = maxQuality;
        baos.reset();
        newbitmap.compress(CompressFormat.JPEG, quality, baos);
        while (baos.toByteArray().length > maxImageSize) {
            quality -= 10;
            baos.reset();
            newbitmap.compress(CompressFormat.JPEG, quality, baos);
        }
        Log.v(LOG_TAG, "Img Compressed: " + quality + "%,  size: " + baos.size() + " bytes, original size: " + origionalSize + " bytes");
        return baos;
    }
}
