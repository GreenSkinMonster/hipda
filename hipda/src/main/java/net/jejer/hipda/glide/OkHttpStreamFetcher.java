package net.jejer.hipda.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.util.ContentLengthInputStream;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

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

/**
 * From glide-okhttp-integration-1.3.1.jar
 * Fetches an {@link InputStream} using the okhttp library.
 */
public class OkHttpStreamFetcher implements DataFetcher<InputStream> {
    private final OkHttpClient client;
    private final GlideUrl url;
    private InputStream stream;
    private ResponseBody responseBody;

    private boolean isForumUrl;
    private String stringUrl;

    public OkHttpStreamFetcher(OkHttpClient client, GlideUrl url) {
        this.client = client;
        this.url = url;
        stringUrl = url.toStringUrl();
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        boolean isAvatarUrl = stringUrl.startsWith(HiUtils.AvatarBaseUrl);
        isForumUrl = isAvatarUrl ? isAvatarUrl : stringUrl.startsWith(HiUtils.BaseUrl);

        return isAvatarUrl ? getAvatar() : getImage();
    }

    private InputStream getImage() throws IOException {
        Request request = getRequest();

        Response response = client.newCall(request).execute();
        responseBody = response.body();
        if (!response.isSuccessful()) {
            throw new IOException("Request failed with code: " + response.code());
        }

        long contentLength = responseBody.contentLength();
        stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
        return stream;
    }

    private InputStream getAvatar() throws IOException {
        File f = GlideHelper.getAvatarFile(stringUrl);
        if (refetch(f)) {
            if (!f.exists() || f.delete()) {
                Request request = getRequest();
                Response response = client.newCall(request).execute();
                responseBody = response.body();

                if (!response.isSuccessful()) {
                    if (response.code() == 404) {
                        GlideHelper.markAvatarNotFound(stringUrl);
                        if (!f.createNewFile())
                            Logger.e("create file failed : " + f.getName());
                    }
                    throw new IOException("Request failed with code: " + response.code());
                }

                InputStream is = response.body().byteStream();

                BufferedInputStream input = null;
                OutputStream output = null;

                try {
                    input = new BufferedInputStream(is);
                    output = new FileOutputStream(f);
                    int count;
                    byte[] data = new byte[1024];
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                } catch (Exception e) {
                    if (f.exists())
                        f.delete();
                } finally {
                    try {
                        if (input != null)
                            input.close();
                        if (output != null)
                            output.close();
                        if (is != null)
                            is.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        } else if (f.length() == 0) {
            GlideHelper.markAvatarNotFound(stringUrl);
            return null;
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
        if (isForumUrl) {
            requestBuilder.removeHeader("User-Agent");
            requestBuilder.header("User-Agent", HiUtils.getUserAgent());
        }

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
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignored
            }
        }
        if (responseBody != null) {
            try {
                responseBody.close();
            } catch (IOException e) {
                // Ignored.
            }
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
