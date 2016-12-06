package net.jejer.hipda.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;

import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * From glide-okhttp-integration-1.3.1.jar
 * Fetches an {@link InputStream} using the okhttp library.
 */
public class AvatarStreamFetcher implements DataFetcher<InputStream> {
    private final OkHttpClient client;
    private final GlideUrl url;
    private ResponseBody responseBody;

    private String stringUrl;

    public AvatarStreamFetcher(OkHttpClient client, GlideUrl url) {
        this.client = client;
        this.url = url;
        stringUrl = url.toStringUrl();
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        File f = GlideHelper.getAvatarFile(stringUrl);
        if (f == null)
            return null;
        if (refetch(f)) {
            if (!f.exists() || f.delete()) {
                Request request = getRequest();
                Response response = client.newCall(request).execute();
                responseBody = response.body();

                if (!response.isSuccessful()) {
                    if (response.code() == 404) {
                        if (!f.createNewFile())
                            Logger.e("create file failed : " + f.getName());
                    } else
                        throw new IOException(OkHttpHelper.ERROR_CODE_PREFIX + response.code());
                } else {
                    InputStream is = response.body().byteStream();
                    BufferedInputStream input = null;
                    OutputStream output = null;

                    try {
                        input = new BufferedInputStream(is);
                        output = new FileOutputStream(f);
                        int count;
                        byte[] data = new byte[2048];
                        while ((count = input.read(data)) != -1) {
                            output.write(data, 0, count);
                        }
                        output.flush();
                    } catch (Exception e) {
                        if (f.exists())
                            f.delete();
                    } finally {
                        try {
                            if (input != null) input.close();
                            if (output != null) output.close();
                            if (is != null) is.close();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        if (!f.exists()) {
            //no memory cahce, avatar will be re-download in next ImageView
            return null;
        } else if (f.length() == 0) {
            //with memory cahce, avatar not found, will be re-download after one day
            GlideHelper.markAvatarNotFound(stringUrl);
            return new FileInputStream(MyGlideModule.DEFAULT_AVATAR_FILE);
        }
        return new FileInputStream(f);
    }

    private Request getRequest() {
        Request.Builder requestBuilder = new Request.Builder()
                .url(stringUrl);

        for (Map.Entry<String, String> headerEntry : url.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }

        //hack, replace User-Agent
        requestBuilder.removeHeader("User-Agent");
        requestBuilder.header("User-Agent", HiUtils.getUserAgent());

        return requestBuilder.build();
    }

    private boolean refetch(File f) {
        //cache avatar for 1 week, cache not found avatar for 1 day
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
    public String getId() {
        return url.getCacheKey();
    }

    @Override
    public void cancel() {
        // TODO: call cancel on the client when this method is called on a background thread. See #257
    }
}
