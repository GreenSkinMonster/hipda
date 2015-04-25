package net.jejer.hipda.glide;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.volley.VolleyUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.signature.StringSignature;

import net.jejer.hipda.R;

import java.io.InputStream;
import java.util.Calendar;

public class GlideHelper {

    private static String WEEK_KEY;

    public static void init(Context context) {
        if (!Glide.isSetup()) {
            GlideBuilder gb = new GlideBuilder(context);
            DiskCache dlw = DiskLruCacheWrapper.get(Glide.getPhotoCacheDir(context), 100 * 1024 * 1024);

            Runtime rt = Runtime.getRuntime();
            long maxMemory = rt.maxMemory();
            gb.setMemoryCache(new LruResourceCache(Math.round(maxMemory * 0.3f)));
            gb.setDiskCache(dlw);

            Glide.setup(gb);

            Glide.get(context).register(GlideUrl.class, InputStream.class, new VolleyUrlLoader.Factory(context));
        }
    }

    public static void loadAvatar(Context ctx, ImageView view, String avatarUrl) {
        if (ctx == null)
            return;
        //use year and week number as cache key
        //avatars will be cache for one week at most
        if (WEEK_KEY == null) {
            Calendar calendar = Calendar.getInstance();
            WEEK_KEY = calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.WEEK_OF_YEAR);
        }
        Glide.with(ctx)
                .load(avatarUrl)
                .signature(new StringSignature(avatarUrl + "_" + WEEK_KEY))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .placeholder(R.drawable.google_user)
                .error(R.drawable.google_user)
                .crossFade()
                .into(view);
    }

}
