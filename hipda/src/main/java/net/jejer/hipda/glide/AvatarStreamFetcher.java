package net.jejer.hipda.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by GreenSkinMonster on 2019-02-25.
 */
public class AvatarStreamFetcher implements DataFetcher<InputStream> {
    private final OkHttpClient client;
    private ResponseBody responseBody;

    private String stringUrl;

    public AvatarStreamFetcher(OkHttpClient client, AvatarModel model) {
        this.client = client;
        this.stringUrl = model.getUrl();
    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
        try {
            File f = GlideHelper.getAvatarFile(stringUrl);
            if (f == null) {
                throw new IOException("cannot get avatar file");
            }
            if (refetch(f)) {
                if (!f.exists() || f.delete()) {
                    Request request = getRequest();
                    Response response = client.newCall(request).execute();
                    responseBody = response.body();

                    if (!response.isSuccessful()) {
                        if (response.code() == 404) {
                            if (!f.createNewFile())
                                Logger.e("create file failed : " + f.getName());
                        }
                    } else {
                        saveAvatar(response, f);
                    }
                }
            }
            if (!f.exists()) {
                //network error, retry next time
                callback.onDataReady(null);
            } else if (f.length() == 0) {
                //avatar not found, cache with zero length file
                callback.onDataReady(new FileInputStream(GlideHelper.DEFAULT_AVATAR_FILE));
            } else {
                callback.onDataReady(new FileInputStream(f));
            }
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    private void saveAvatar(Response response, File f) {
        try (InputStream is = response.body().byteStream();
             BufferedInputStream input = new BufferedInputStream(is);
             OutputStream output = new FileOutputStream(f)) {
            int count;
            byte[] data = new byte[4096];
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
        } catch (Exception e) {
            if (f.exists())
                f.delete();
        }
    }

    private Request getRequest() {
        Request.Builder requestBuilder = new Request.Builder()
                .url(stringUrl);

        //hack, replace User-Agent
        requestBuilder.removeHeader("User-Agent");
        requestBuilder.header("User-Agent", HiUtils.getUserAgent());

        return requestBuilder.build();
    }

    private boolean refetch(File f) {
        //cache avatar for 3 days, cache not found avatar for 1 day
        return !f.exists()
                || (f.length() > 0 && f.lastModified() < System.currentTimeMillis() - GlideHelper.AVATAR_CACHE_MILLS)
                || (f.length() == 0 && f.lastModified() < System.currentTimeMillis() - GlideHelper.AVATAR_404_CACHE_MILLS);
    }

    @Override
    public void cleanup() {
        if (responseBody != null) {
            responseBody.close();
        }
    }

    @Override
    public void cancel() {
    }
}
