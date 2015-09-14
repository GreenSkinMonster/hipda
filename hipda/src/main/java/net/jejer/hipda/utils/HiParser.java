package net.jejer.hipda.utils;

import android.content.Context;
import android.text.TextUtils;

import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.bean.UserInfoBean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class HiParser {

    public static SimpleListBean parseSimpleList(Context ctx, int type, Document doc) {

        // Async check notify
        new HiParserThreadList.parseNotifyRunnable(ctx, doc).run();

        switch (type) {
            case SimpleListLoader.TYPE_MYREPLY:
                return parseReplyList(ctx, doc);
            case SimpleListLoader.TYPE_MYPOST:
                return parseMyPost(ctx, doc);
            case SimpleListLoader.TYPE_SMS:
                return parseSMS(doc);
            case SimpleListLoader.TYPE_THREAD_NOTIFY:
                return parseNotify(doc);
            case SimpleListLoader.TYPE_SMS_DETAIL:
                return parseSmsDetail(doc);
            case SimpleListLoader.TYPE_SEARCH:
                return parseSearch(doc);
            case SimpleListLoader.TYPE_SEARCH_USER_THREADS:
                return parseSearch(doc);
            case SimpleListLoader.TYPE_FAVORITES:
                return parseFavorites(doc);
            case SimpleListLoader.TYPE_ATTENTION:
                return parseFavorites(doc);
        }

        return null;
    }

    private static SimpleListBean parseReplyList(Context ctx, Document doc) {
        if (doc == null) {
            return null;
        }

        Elements tableES = doc.select("table.datatable");
        if (tableES.size() == 0) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        int last_page = 1;
        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        if (pagesES.size() > 0) {
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements trES = tableES.first().select("tr");

        SimpleListItemBean item = null;
        //first tr is title, skip
        for (int i = 1; i < trES.size(); ++i) {
            Element trE = trES.get(i);

            // odd have title, even have reply text;
            if (i % 2 == 1) {
                item = new SimpleListItemBean();

                // thread
                Elements thES = trE.select("th");
                if (thES.size() == 0) {
                    continue;
                }
                Elements linkES = thES.first().select("a");
                if (linkES.size() != 1) {
                    continue;
                }
                String tid = linkES.first().attr("href");
                if (!tid.startsWith("redirect.php?goto=")) {
                    continue;
                }
                item.setTid(HttpUtils.getMiddleString(tid, "ptid=", "&"));
                item.setPid(HttpUtils.getMiddleString(tid, "pid=", "&"));
                String title = linkES.first().text();

                // time
                Elements lastpostES = trE.select("td.lastpost");
                if (lastpostES.size() == 0) {
                    continue;
                }
                String time = lastpostES.first().text();

                item.setTitle(title);
                item.setTime(time);

                Elements forumES = trE.select("td.forum");
                if (forumES.size() > 0) {
                    item.setForum(forumES.first().text());
                }

            } else {
                list.add(item);

                Elements thES = trE.select("th");
                if (thES.size() == 0) {
                    continue;
                }
                item.setInfo(thES.first().text());
            }
        }
        return list;
    }

    private static SimpleListBean parseMyPost(Context ctx, Document doc) {
        if (doc == null) {
            return null;
        }

        Elements tableES = doc.select("table.datatable");
        if (tableES.size() == 0) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        int last_page = 1;
        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        if (pagesES.size() > 0) {
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements trES = tableES.first().select("tr");

        SimpleListItemBean item = null;
        //first tr is title, skip
        for (int i = 1; i < trES.size(); ++i) {
            Element trE = trES.get(i);

            // odd have title, even have reply text;
            item = new SimpleListItemBean();

            // thread
            Elements thES = trE.select("th");
            if (thES.size() == 0) {
                continue;
            }
            Elements linkES = thES.first().select("a");
            if (linkES.size() != 1) {
                continue;
            }
            String tid = linkES.first().attr("href");
            if (!tid.startsWith("viewthread.php?tid=")) {
                continue;
            }
            tid = HttpUtils.getMiddleString(tid, "viewthread.php?tid=", "&");
            String title = linkES.first().text();

            // time
            Elements lastpostES = trE.select("td.lastpost");
            if (lastpostES.size() == 0) {
                continue;
            }
            String time = lastpostES.first().text();

            item.setTid(tid);
            item.setTitle(title);
            item.setTime(time);

            Elements forumES = trE.select("td.forum");
            if (forumES.size() > 0) {
                item.setForum(forumES.first().text());
            }

            list.add(item);
        }
        return list;
    }

    public static SimpleListBean parseSMS(Document doc) {
        if (doc == null) {
            return null;
        }

        Elements pmlistES = doc.select("ul.pm_list");
        if (pmlistES.size() < 1) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        Elements liES = pmlistES.first().select("li");
        for (int i = 0; i < liES.size(); ++i) {
            Element liE = liES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            // avatar
            Elements avatarES = liE.select("a.avatar");
            if (avatarES.size() > 0) {
                Elements avatarImgES = avatarES.first().select("img");
                if (avatarImgES.size() > 0) {
                    item.setAvatarUrl(avatarImgES.first().attr("src"));
                }
            }

            // author and author uid
            Elements pciteES = liE.select("p.cite");
            if (pciteES.size() == 0) {
                continue;
            }
            Elements citeES = pciteES.first().select("cite");
            if (citeES.size() == 0) {
                continue;
            }
            item.setAuthor(citeES.first().text());
            item.setForum(item.getAuthor());
            Elements uidAES = citeES.first().select("a");
            if (uidAES.size() == 0) {
                continue;
            }
            String uid = uidAES.first().attr("href");
            item.setUid(HttpUtils.getMiddleString(uid, "uid=", "&"));

            // time
            item.setTime(pciteES.first().ownText());

            // new
            Elements imgES = pciteES.first().select("img");
            if (imgES.size() > 0) {
                if (imgES.first().attr("src").equals("images/default/notice_newpm.gif")) {
                    item.setNew(true);
                }
            }

            // info
            Elements summaryES = liE.select("div.summary");
            if (summaryES.size() == 0) {
                continue;
            }
            item.setTitle(summaryES.first().text());

            list.add(item);
        }

        return list;
    }

    public static SimpleListBean parseNotify(Document doc) {
        if (doc == null) {
            return null;
        }

        Elements feedES = doc.select("ul.feed");
        if (feedES.size() == 0) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        Elements liES = feedES.first().select("li");
        for (int i = 0; i < liES.size(); ++i) {
            Element liE = liES.get(i);
            Elements divES = liE.select("div");
            if (divES.size() == 0) {
                continue;
            }
            SimpleListItemBean item = null;
            if (divES.first().hasClass("f_thread")) {
                // user reply your thread
                item = parseNotifyThread(divES.first());
            } else if (divES.first().hasClass("f_quote")) {
                // user quote your post
                item = parseNotifyQuoteandReply(divES.first());
            } else if (divES.first().hasClass("f_reply")) {
                // user reply your post
                item = parseNotifyQuoteandReply(divES.first());
            }

            if (item != null) {
                list.add(item);
            }
        }

        return list;
    }

    public static SimpleListItemBean parseNotifyThread(Element root) {
        SimpleListItemBean item = new SimpleListItemBean();
        String info = "";

        Elements aES = root.select("a");
        for (Element a : aES) {
            String href = a.attr("href");
            if (href.contains("space.php")) {
                // get replied usernames
                info += a.text() + " ";
            } else if (href.startsWith(HiUtils.BaseUrl + "redirect.php?from=notice&goto=findpost")) {
                // Thread Name and TID and PID
                item.setTitle(a.text());
                item.setTid(HttpUtils.getMiddleString(a.attr("href"), "ptid=", "&"));
                item.setPid(HttpUtils.getMiddleString(a.attr("href"), "pid=", "&"));
                break;
            }
        }

        // time
        Elements emES = root.select("em");
        if (emES.size() == 0) {
            return null;
        }
        item.setTime(emES.first().text());

        if (root.text().contains("回复了您关注的主题"))
            info += "回复了您关注的主题";
        else
            info += "回复了您的帖子 ";

        // new
        Elements imgES = root.select("img");
        if (imgES.size() > 0) {
            if (imgES.first().attr("src").equals("images/default/notice_newpm.gif")) {
                item.setNew(true);
            }
        }

        item.setInfo(info);
        return item;
    }

    public static SimpleListItemBean parseNotifyQuoteandReply(Element root) {
        SimpleListItemBean item = new SimpleListItemBean();


        Elements aES = root.select("a");
        for (Element a : aES) {
            String href = a.attr("href");
            if (href.startsWith(HiUtils.BaseUrl + "space.php")) {
                String uid = HttpUtils.getMiddleString(a.attr("href"), "uid=", "&");
                item.setAuthor(a.text());
                item.setAvatarUrl(HiUtils.getAvatarUrlByUid(uid));
            } else if (href.startsWith(HiUtils.BaseUrl + "viewthread.php")) {
                // Thread Name and TID and PID
                item.setTitle(a.text());
            } else if (href.startsWith(HiUtils.BaseUrl + "redirect.php?from=notice&goto=findpost")) {
                // Thread Name and TID and PID
                item.setTid(HttpUtils.getMiddleString(a.attr("href"), "ptid=", "&"));
                item.setPid(HttpUtils.getMiddleString(a.attr("href"), "pid=", "&"));
            }
        }

        // time
        Elements emES = root.select("em");
        if (emES.size() == 0) {
            return null;
        }
        item.setTime(item.getAuthor() + " " + emES.first().text());

        // summary
        String info = "";
        Elements summaryES = root.select(".summary");
        if (summaryES.size() > 0) {
            Elements ddES = summaryES.select("dd");
            if (ddES.size() == 2) {
                info = "<u>您的帖子:</u>" + ddES.get(0).text();
                info += "<br><u>" + item.getAuthor() + " 说:</u>" + ddES.get(1).text();
            } else {
                info = summaryES.first().text();
            }
        }

        // new
        Elements imgES = root.select("img");
        if (imgES.size() > 0) {
            if (imgES.first().attr("src").equals("images/default/notice_newpm.gif")) {
                item.setNew(true);
            }
        }

        item.setInfo(info);
        return item;
    }

    private static SimpleListBean parseSmsDetail(Document doc) {
        if (doc == null) {
            return null;
        }

        //get my uid and username
        Elements uidMenuES = doc.select("#umenu cite a.noborder");
        if (uidMenuES.size() < 1) {
            return null;
        }
        String mySpaceUrl = Utils.nullToText(uidMenuES.first().attr("href"));
        String myUid = HttpUtils.getMiddleString(mySpaceUrl, "uid=", "&");
        String myUsername = uidMenuES.first().text();

        Elements smslistES = doc.select("li.s_clear");
        if (smslistES.size() < 1) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        for (int i = 0; i < smslistES.size(); ++i) {
            Element smsE = smslistES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            // author
            Elements pciteES = smsE.select("p.cite");
            if (pciteES.size() == 0) {
                continue;
            }
            Elements citeES = pciteES.first().select("cite");
            if (citeES.size() == 0) {
                continue;
            }
            item.setAuthor(citeES.first().text());

            // avatar
            Elements avatarES = smsE.select("a.avatar");
            if (avatarES.size() > 0) {
                if (item.getAuthor().equals(myUsername)) {
                    item.setUid(myUid);
                } else {
                    String spaceUrl = Utils.nullToText(avatarES.first().attr("href"));
                    item.setUid(HttpUtils.getMiddleString(spaceUrl, "uid=", "&"));
                }
                item.setAvatarUrl(HiUtils.getAvatarUrlByUid(item.getUid()));
            }

            // time
            item.setTime(pciteES.first().ownText());

            // info
            Elements summaryES = smsE.select("div.summary");
            if (summaryES.size() == 0) {
                continue;
            }
            item.setInfo(summaryES.first().html());

            // new
            Elements imgES = pciteES.first().select("img");
            if (imgES.size() > 0) {
                if (imgES.first().attr("src").equals("images/default/notice_newpm.gif")) {
                    item.setNew(true);
                }
            }

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseSearch(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        int last_page = 1;

        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        String searchIdUrl;
        if (pagesES.size() > 0) {
            searchIdUrl = pagesES.first().attr("href");
            if (searchIdUrl.contains("srchtype=fulltext")) {
                return parseSearchFullText(doc);
            }
            list.setSearchIdUrl(searchIdUrl);
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements tbodyES = doc.select("tbody");
        for (int i = 0; i < tbodyES.size(); ++i) {
            Element tbodyE = tbodyES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            Elements subjectES = tbodyE.select("tr th.subject");
            if (subjectES.size() == 0) {
                continue;
            }
            item.setTitle(subjectES.first().text());

            Elements subjectAES = subjectES.first().select("a");
            if (subjectAES.size() == 0) {
                continue;
            }
            String href = subjectAES.first().attr("href");
            item.setTid(HttpUtils.getMiddleString(href, "tid=", "&"));

            Elements authorAES = tbodyE.select("tr td.author cite a");
            if (authorAES.size() == 0) {
                continue;
            }
            item.setAuthor(authorAES.first().text());

            String spaceUrl = authorAES.first().attr("href");
            if (!TextUtils.isEmpty(spaceUrl)) {
                String uid = HttpUtils.getMiddleString(spaceUrl, "uid=", "&");
                item.setAvatarUrl(HiUtils.getAvatarUrlByUid(uid));
            }

            Elements timeES = tbodyE.select("tr td.author em");
            if (timeES.size() > 0) {
                item.setTime(item.getAuthor() + "  " + timeES.first().text());
            }

            Elements forumES = tbodyE.select("tr td.forum");
            if (forumES.size() > 0) {
                item.setForum(forumES.first().text());
            }

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseSearchFullText(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
        int last_page = 1;

        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages_btns div.pages a");
        pagesES.addAll(doc.select("div.pages_btns div.pages strong"));
        String searchIdUrl;
        if (pagesES.size() > 0) {
            searchIdUrl = pagesES.first().attr("href");
            list.setSearchIdUrl(searchIdUrl);
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements tbodyES = doc.select("table.datatable tr");
        for (int i = 0; i < tbodyES.size(); ++i) {
            Element trowE = tbodyES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            Elements subjectES = trowE.select("div.sp_title a");
            if (subjectES.size() == 0) {
                continue;
            }
            item.setTitle(subjectES.first().text());
            //gotopost.php?pid=12345
            String postUrl = Utils.nullToText(subjectES.first().attr("href"));
            item.setPid(HttpUtils.getMiddleString(postUrl, "pid=", "&"));
            if (TextUtils.isEmpty(item.getPid())) {
                continue;
            }

            Elements contentES = trowE.select("div.sp_content");
            if (contentES.size() > 0) {
                item.setInfo(contentES.text());
            }

//            <div class="sp_theard">
//            <span class="sp_w200">版块: <a href="forumdisplay.php?fid=2">Discovery</a></span>
//            <span>作者: <a href="space.php?uid=189027">tsonglin</a></span>
//            <span>查看: 1988</span>
//            <span>回复: 56</span>
//            <span class="sp_w200">最后发表: 2015-4-4 21:58</span>
//            </div>
            Elements postInfoES = trowE.select("div.sp_theard span");
            if (postInfoES.size() != 5) {
                continue;
            }
            Elements authorES = postInfoES.get(1).select("a");
            if (authorES.size() > 0) {
                item.setAuthor(authorES.first().text());
                String spaceUrl = authorES.first().attr("href");
                if (!TextUtils.isEmpty(spaceUrl)) {
                    String uid = HttpUtils.getMiddleString(spaceUrl, "uid=", "&");
                    item.setAvatarUrl(HiUtils.getAvatarUrlByUid(uid));
                }
            }

            item.setTime(item.getAuthor() + " " + HttpUtils.getMiddleString(postInfoES.get(4).text(), ":", "&"));

            Elements forumES = postInfoES.get(0).select("a");
            if (forumES.size() > 0)
                item.setForum(forumES.first().text());

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseFavorites(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();

        int last_page = 1;
        //if this is the last page, page number is in <strong>
        Elements pagesES = doc.select("div.pages a");
        pagesES.addAll(doc.select("div.pages strong"));
        if (pagesES.size() > 0) {
            for (Node n : pagesES) {
                int tmp = HttpUtils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
            }
        }
        list.setMaxPage(last_page);

        Elements trES = doc.select("table.datatable tbody tr");
        for (int i = 0; i < trES.size(); ++i) {
            Element trE = trES.get(i);
            SimpleListItemBean item = new SimpleListItemBean();

            Elements subjectES = trE.select("th");
            if (subjectES.size() == 0) {
                continue;
            }
            item.setTitle(subjectES.first().text());

            Elements subjectAES = subjectES.first().select("a");
            if (subjectAES.size() == 0) {
                continue;
            }
            String href = subjectAES.first().attr("href");
            item.setTid(HttpUtils.getMiddleString(href, "tid=", "&"));

            Elements timeES = trE.select("td.lastpost");
            if (timeES.size() > 0) {
                item.setTime(timeES.first().text().trim());
            }

            Elements forumES = trE.select("td.forum");
            if (forumES.size() > 0) {
                item.setForum(forumES.first().text().trim());
            }

            list.add(item);
        }

        return list;
    }

    public static UserInfoBean parseUserInfo(String rsp) {
        Document doc = Jsoup.parse(rsp);
        if (doc == null) {
            return null;
        }

        UserInfoBean info = new UserInfoBean();

        Elements usernameES = doc.select("div#profilecontent div.itemtitle h1");
        if (usernameES.size() > 0) {
            info.setUsername(Utils.nullToText(usernameES.first().text()).trim());
        }

        Elements onlineImgES = doc.select("div#profilecontent div.itemtitle img");
        if (onlineImgES.size() > 0) {
            info.setOnline(Utils.nullToText(onlineImgES.first().attr("src")).contains("online"));
        }

        Elements uidES = doc.select("div#profilecontent div.itemtitle ul li");
        if (uidES.size() > 0) {
            info.setUid(HttpUtils.getMiddleString(uidES.first().text(), "(UID:", ")").trim());
        }

        Elements avatarES = doc.select("div.side div.profile_side div.avatar img");
        if (avatarES.size() != 0) {
            info.setAvatarUrl(avatarES.first().attr("src"));
        }

        StringBuilder sb = new StringBuilder();

        Elements titleES = doc.select("h3.blocktitle");
        int i = 0;
        for (Element titleEl : titleES) {
            sb.append(titleEl.text()).append("\n\n");
            if (i == 0) {
                Elements detailES = doc.select("div.main div.s_clear ul.commonlist li");
                for (Element detail : detailES) {
                    sb.append(detail.text()).append('\n');
                }
            }
            i++;
            sb.append("\n");
            if (i >= 2)
                break;
        }


        info.setDetail(sb.toString());

        return info;
    }
}
