package net.jejer.hipda.utils;

import android.content.Context;
import android.text.TextUtils;

import com.vdurmont.emoji.EmojiParser;

import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.service.NotiHelper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HiParserThreadList {

    private static long HOLD_FETCH_NOTIFY = 0;

    public static ThreadListBean parse(Context context, Document doc) {

        new Thread(new ParseNotifyRunnable(context, doc)).start();
        HiSettingsHelper.updateMobileNetworkStatus(context);

        ThreadListBean threads = new ThreadListBean();

        //parse uid and re-set username if necessary
        if (TextUtils.isEmpty(HiSettingsHelper.getInstance().getUid())) {
            Elements spaceES = doc.select("#umenu cite a");
            if (spaceES.size() == 1) {
                String spaceUrl = spaceES.first().attr("href");
                if (!TextUtils.isEmpty(spaceUrl)) {
                    String uid = Utils.getMiddleString(spaceUrl, "space.php?uid=", "&");
                    String username = Utils.nullToText(spaceES.first().text()).trim();
                    if (!TextUtils.isEmpty(uid)
                            && TextUtils.isDigitsOnly(uid)
                            && !HiSettingsHelper.getInstance().getUid().equals(uid)) {
                        //re-set username if it is not exactly SAME to user inputted with case sensitive
                        if (!HiSettingsHelper.getInstance().getUsername().equals(username)
                                && HiSettingsHelper.getInstance().getUsername().equalsIgnoreCase(username))
                            HiSettingsHelper.getInstance().setUsername(username);
                        //uid will be setted later
                        threads.setUid(uid);
                    }
                }
            }
        }

        Elements tbodyES = doc.select("tbody[id]");
        for (int i = 0; i < tbodyES.size(); ++i) {

            threads.setParsed(true);

            Element tbodyE = tbodyES.get(i);
            ThreadBean thread = new ThreadBean();

            /* title and tid */
            String[] idSpil = tbodyE.attr("id").split("_");
            if (idSpil.length != 2) {
                continue;
            }
            String idType = idSpil[0];
            String idNum = idSpil[1];

            thread.setTid(idNum);
            // is stick thread or normal thread
            boolean isStick = idType.startsWith("stickthread");
            Elements images = tbodyE.select("img");
            if (images.size() > 0) {
                String src = images.get(0).attr("src");
                isStick = src.contains("/pin");
            }
            thread.setIsStick(isStick);

            if (isStick && !HiSettingsHelper.getInstance().isShowStickThreads()) {
                continue;
            }

            Elements titleES = tbodyE.select("span#thread_" + idNum + " a");
            if (titleES.size() == 0) {
                continue;
            }
            Element titleLink = titleES.first();
            String title = titleLink.text();
            thread.setTitle(EmojiParser.parseToUnicode(title));

            String linkStyle = titleLink.attr("style");
            if (!TextUtils.isEmpty(linkStyle)) {
                thread.setTitleColor(Utils.getMiddleString(linkStyle, "color:", "").trim());
            }

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
            String authorId = Utils.getMiddleString(userLink, "uid=", "&");
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

            //get max page number
            Elements pages = tbodyE.select("span.threadpages a");
            int maxPage = 1;
            if (pages.size() > 0) {
                Element pageLink = pages.get(pages.size() - 1);
                if (!TextUtils.isEmpty(pageLink.attr("href"))) {
                    String lastPage = Utils.getMiddleString(pageLink.attr("href"), "page=", "&");
                    if (!TextUtils.isEmpty(lastPage) && TextUtils.isDigitsOnly(lastPage))
                        maxPage = Utils.parseInt(lastPage);
                }
            }
            thread.setMaxPage(maxPage);

            threads.add(thread);
        }

        return threads;
    }

    public static class ParseNotifyRunnable implements Runnable {

        private Document mDoc;
        private Context mCtx;

        public ParseNotifyRunnable(Context context, Document doc) {
            mDoc = doc;
            mCtx = context;
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() > HOLD_FETCH_NOTIFY + 10 * 1000) {
                NotiHelper.fetchNotification(mDoc);
                NotiHelper.showNotification(mCtx);
            }
        }
    }

    public static void holdFetchNotify() {
        HOLD_FETCH_NOTIFY = System.currentTimeMillis();
    }

}
