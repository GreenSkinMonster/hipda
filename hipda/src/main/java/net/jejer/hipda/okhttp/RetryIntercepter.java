package net.jejer.hipda.okhttp;

import net.jejer.hipda.utils.Logger;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RetryIntercepter implements Interceptor {

    private final static int MAX_TRY = 3;
    private final static int MAX_TRY_SECS = 20;

    public RetryIntercepter() {
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();

        long start = System.currentTimeMillis();
        int tryNum = 1;

        Response response = null;
        while (response == null && tryNum <= MAX_TRY) {
            if (Logger.isDebug())
                Logger.v(url + ", try#" + tryNum);
            try {
                if (tryNum > 0) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) {
                    }
                }
                response = chain.proceed(request);
                if (Logger.isDebug())
                    Logger.v(url + ", try#" + tryNum + ", " + response.code());
            } catch (IOException e) {
                Logger.v(url + ", try#" + tryNum + ", "
                        + e.getClass().getName() + " : " + e.getMessage());
                if (tryNum == MAX_TRY
                        || !"GET".equalsIgnoreCase(request.method())
                        || (System.currentTimeMillis() - start) > 1000 * MAX_TRY_SECS) {
                    throw e;
                }
            }
            tryNum++;
        }
        return response;
    }

}
