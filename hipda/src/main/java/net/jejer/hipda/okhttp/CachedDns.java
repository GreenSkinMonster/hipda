package net.jejer.hipda.okhttp;

import androidx.annotation.NonNull;

import net.jejer.hipda.utils.HiUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Dns;

public class CachedDns implements Dns {

    private final static Map<String, List<InetAddress>> CACHE = new HashMap<>();
    private static CachedDns INSTANCE = null;

    private CachedDns() {
    }

    public static CachedDns getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CachedDns();
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        List<InetAddress> ips = CACHE.get(hostname);
        if (ips == null) {
            ips = Dns.SYSTEM.lookup(hostname);
            if (hostname.endsWith(HiUtils.CookieDomain) && ips.size() > 0)
                CACHE.put(hostname, ips);
        }
        return ips;
    }

    public void clearCache() {
        CACHE.clear();
    }

}
