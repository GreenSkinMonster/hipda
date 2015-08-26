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


public class FavoriteHelper {

    private FavoriteHelper() {
    }

    private static class SingletonHolder {
        public static final FavoriteHelper INSTANCE = new FavoriteHelper();
    }

    public static FavoriteHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addFavorite(final Context ctx, final String tid, final String title) {
        StringRequest sReq = new HiStringRequest(HiUtils.FavoriteAddUrl + tid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("此主题已成功添加到收藏夹中")
                                || response.contains("您曾经收藏过这个主题")) {
                            Toast.makeText(ctx, title + " 收藏添加成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ctx, title + " 收藏添加失败, 请重试", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logger.e(error);
                        Toast.makeText(ctx, title + " 收藏添加失败, 请重试." + VolleyHelper.getErrorReason(error), Toast.LENGTH_LONG).show();
                    }
                });
        VolleyHelper.getInstance().add(sReq);
    }

    public void removeFavorite(final Context ctx, final String tid, final String title) {
        StringRequest sReq = new HiStringRequest(HiUtils.FavoriteRemoveUrl + tid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("此主题已成功从您的收藏夹中移除")) {
                            Toast.makeText(ctx, title + " 收藏删除成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ctx, title + " 收藏删除失败, 请重试", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logger.e(error);
                        Toast.makeText(ctx, title + " 收藏删除失败, 请重试." + VolleyHelper.getErrorReason(error), Toast.LENGTH_LONG).show();
                    }
                });
        VolleyHelper.getInstance().add(sReq);
    }
}
