package net.jejer.hipda.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.async.HiStringRequest;
import net.jejer.hipda.async.VolleyHelper;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.ui.NotifyHelper;
import net.jejer.hipda.ui.ThreadListFragment;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;

public class HiParserThreadList {

    public static ThreadListBean parse(Context ctx, Handler handler, Document doc) {
        // Update UI
        Message msgStartParse = Message.obtain();
        msgStartParse.what = ThreadListFragment.STAGE_PARSE;
        handler.sendMessage(msgStartParse);

        // Async check notify
        new parseNotifyRunnable(ctx, doc, true).run();

        //get cdn image host from image url
        if (!HiUtils.ImageHostUpdated) {
            Elements newES = doc.select("#newspecial img");
            if (newES.size() > 0) {
                String src = newES.first().attr("src");
                try {
                    String host = (new URL(src)).getHost();
                    HiUtils.updateImageHost(host);
                    HiUtils.ImageHostUpdated = true;
                } catch (Exception ignored) {
                }
            }
        }


        ThreadListBean threads = new ThreadListBean();
        Elements tbodyES = doc.select("tbody[id]");
        for (int i = 0; i < tbodyES.size(); ++i) {

            threads.parsed = true;

            Element tbodyE = tbodyES.get(i);
            ThreadBean thread = new ThreadBean();

			/* title and tid */
            String[] idSpil = tbodyE.attr("id").split("_");
            if (idSpil.length != 2) {
                continue;
            }
            String idType = idSpil[0];
            String idNum = idSpil[1];
            String idThread = "thread_" + idNum;
            thread.setTid(idNum);
            // is stick thread or normal thread
            Boolean isStick = idType.startsWith("stickthread");
            thread.setIsStick(isStick);

            if (isStick && !HiSettingsHelper.getInstance().isShowStickThreads()) {
                continue;
            }

            Elements titleES = tbodyE.select("span#" + idThread);
            if (titleES.size() == 0) {
                continue;
            }
            String title = titleES.first().text();
            thread.setTitle(title);

            Elements typeES = tbodyE.select("th.subject em a");
            if (typeES.size() > 0) {
                thread.setType(typeES.text());
            }

            Elements threadIsNewES = tbodyE.select("td.folder img");
            if (threadIsNewES.size() > 0) {
                String imgSrc = Utils.nullToText(threadIsNewES.first().attr("src"));
                thread.setNew(imgSrc.contains("new"));
            }

			/*  author, authorId and create_time  */
            Elements authorES = tbodyE.select("td.author");
            if (authorES.size() == 0) {
                continue;
            }
            Elements authorciteAES = authorES.first().select("cite a");
            if (authorciteAES.size() == 0) {
                continue;
            }
            String author = authorciteAES.first().text();
            if (!thread.setAuthor(author)) {
                //author in blacklist
                continue;
            }

            String userLink = authorciteAES.first().attr("href");
            if (userLink.length() < "space.php?uid=".length()) {
                continue;
            }
            String authorId = HttpUtils.getMiddleString(userLink, "uid=", "&");
            thread.setAuthorId(authorId);

            thread.setAvatarUrl(HiUtils.getAvatarUrlByUid(authorId));

            Elements threadCreateTimeES = authorES.first().select("em");
            if (threadCreateTimeES.size() == 0) {
                continue;
            }
            String threadCreateTime = threadCreateTimeES.first().text();
            thread.setTimeCreate(threadCreateTime);

            Elements threadUpdateTimeES = tbodyE.select("td.lastpost em a");
            if (threadUpdateTimeES.size() > 0) {
                String threadUpdateTime = threadUpdateTimeES.first().text();
                thread.setTimeUpdate(threadUpdateTime);
            }

			/*  comments and views  */
            Elements nums = tbodyE.select("td.nums");
            if (nums.size() == 0) {
                continue;
            }
            Elements comentsES = nums.first().select("strong");
            if (comentsES.size() == 0) {
                continue;
            }
            String comments = comentsES.first().text();
            thread.setCountCmts(comments);

            Elements viewsES = nums.first().select("em");
            if (viewsES.size() == 0) {
                continue;
            }
            String views = viewsES.first().text();
            thread.setCountViews(views);

            // lastpost
            Elements lastpostciteES = tbodyE.select("td.lastpost cite");
            if (lastpostciteES.size() == 0) {
                continue;
            }
            String lastpost = lastpostciteES.first().text();
            thread.setLastPost(lastpost);

            // attachment and picture
            Elements attachs = tbodyE.select("img.attach");
            for (int j = 0; j < attachs.size(); j++) {
                Element attach = attachs.get(j);
                String attach_img_url = attach.attr("src");
                if (attach_img_url.isEmpty()) {
                    continue;
                }
                if (attach_img_url.endsWith("image_s.gif")) {
                    thread.setHavePic(true);
                }
                if (attach_img_url.endsWith("common.gif")) {
                    thread.setHaveAttach(true);
                }
            }

            threads.add(thread);
        }

        return threads;
    }

    public static class parseNotifyRunnable implements Runnable {
        private Context mCtx;
        private Document mDoc;
        private boolean mCheckSMS;

        public parseNotifyRunnable(Context ctx, Document doc, boolean checkSMS) {
            mCtx = ctx;
            mDoc = doc;
            mCheckSMS = checkSMS;
        }

        @Override
        public void run() {
            Elements promptcontentES = mDoc.select("div.promptcontent");
            if (promptcontentES.size() < 1) {
                return;
            }

            String notifyStr = promptcontentES.first().text();
            //私人消息 (1) 公共消息 (0) 系统消息 (0) 好友消息 (0) 帖子消息 (0)
            int cnt = 0;
            for (String s : notifyStr.split("\\) ")) {
                if (s.contains("私人消息")) {
                    cnt = HttpUtils.getIntFromString(s);
                    NotifyHelper.getInstance().setCntSMS(cnt);
                    Logger.v("NEW SMS:" + String.valueOf(cnt));
                } else if (s.contains("帖子消息")) {
                    cnt = HttpUtils.getIntFromString(s);
                    NotifyHelper.getInstance().setCntThread(cnt);
                    Logger.v("THREAD NOTIFY:" + String.valueOf(cnt));
                }
            }

            if (mCheckSMS) {
                // Trigger Refresh SMS, result will show in next load.
                StringRequest sReq = new HiStringRequest(HiUtils.CheckSMS,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                            }
                        }, null);
                VolleyHelper.getInstance().add(sReq);
            }

            // Update UI
            NotifyHelper.getInstance().updateDrawer();
        }
    }
}
