package net.jejer.hipda.async;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.utils.HiUtils;
import net.jejer.hipda.utils.Logger;
import net.jejer.hipda.volley.HiStringRequest;
import net.jejer.hipda.volley.VolleyHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;


public class FavoriteHelper {

    public final static String TYPE_FAVORITE = "favorites";
    public final static String TYPE_ATTENTION = "attention";

    private FavoriteHelper() {
    }

    private static class SingletonHolder {
        public static final FavoriteHelper INSTANCE = new FavoriteHelper();
    }

    public static FavoriteHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addFavorite(final Context ctx, String item, final String tid) {
        String url = HiUtils.FavoriteAddUrl.replace("{item}", item).replace("{tid}", tid);
        StringRequest sReq = new HiStringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String result = "";
                        Document doc = Jsoup.parse(response, "", Parser.xmlParser());
                        for (Element e : doc.select("root")) {
                            result = e.text();
                            if (result.contains("<"))
                                result = result.substring(0, result.indexOf("<"));
                        }
                        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logger.e(error);
                        Toast.makeText(ctx, " 添加失败 : " + VolleyHelper.getErrorReason(error), Toast.LENGTH_LONG).show();
                    }
                });
        VolleyHelper.getInstance().add(sReq);
    }

    public void removeFavorite(final Context ctx, String item, final String tid) {
        String url = HiUtils.FavoriteRemoveUrl.replace("{item}", item).replace("{tid}", tid);
        StringRequest sReq = new HiStringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String result = "";
                        Document doc = Jsoup.parse(response, "", Parser.xmlParser());
                        for (Element e : doc.select("root")) {
                            result = e.text();
                            if (result.contains("<"))
                                result = result.substring(0, result.indexOf("<"));
                        }
                        Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logger.e(error);
                        Toast.makeText(ctx, "删除失败 : " + VolleyHelper.getErrorReason(error), Toast.LENGTH_LONG).show();
                    }
                });
        VolleyHelper.getInstance().add(sReq);
    }
}
