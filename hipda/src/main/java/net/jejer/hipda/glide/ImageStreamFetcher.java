package net.jejer.hipda.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.util.ContentLengthInputStream;

import net.jejer.hipda.okhttp.OkHttpHelper;
import net.jejer.hipda.utils.HiUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * From glide-okhttp-integration-1.3.1.jar
 * Fetches an {@link InputStream} using the okhttp library.
 */
public class ImageStreamFetcher implements DataFetcher<InputStream> {
    private final OkHttpClient client;
    private final GlideUrl url;
    private InputStream stream;
    private ResponseBody responseBody;

    private boolean isForumUrl;
    private String stringUrl;

    public ImageStreamFetcher(OkHttpClient client, GlideUrl url, boolean isForumUrl) {
        this.client = client;
        this.url = url;
        this.isForumUrl = isForumUrl;
        stringUrl = url.toStringUrl();
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
        Request request = getRequest();

        Response response = client.newCall(request).execute();
        responseBody = response.body();
        if (!response.isSuccessful()) {
            throw new IOException(OkHttpHelper.ERROR_CODE_PREFIX + response.code());
        }

        long contentLength = responseBody.contentLength();
        stream = ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
        return stream;
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
