package net.jejer.hipda.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

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
    public static final String LOG_TAG = "HiParser";

    public static SimpleListBean parseSimpleList(Context ctx, int type, Document doc) {
        switch (type) {
            case SimpleListLoader.TYPE_MYREPLY:
                return parseReplyList(ctx, doc);
            case SimpleListLoader.TYPE_MYPOST:
                return parseMyPost(ctx, doc);
            case SimpleListLoader.TYPE_SMS:
                return parseSMS(doc);
            case SimpleListLoader.TYPE_THREADNOTIFY:
                return parseNotify(doc);
            case SimpleListLoader.TYPE_SMSDETAIL:
                return parseSmsDetail(doc);
            case SimpleListLoader.TYPE_SEARCH:
                return parseSearch(doc);
            case SimpleListLoader.TYPE_SEARCH_USER_THREADS:
                return parseSearch(doc);
            case SimpleListLoader.TYPE_FAVORITES:
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
                tid = HttpUtils.getMiddleString(tid, "ptid=", "&");
                String title = linkES.first().text();

                // time
                Elements lastpostES = trE.select("td.lastpost");
                if (lastpostES.size() == 0) {
                    continue;
                }
                String time = lastpostES.first().text();

                item.setId(tid);
                item.setTitle(title);
                item.setTime(time);
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
        Elements trES = tableES.first().select("tr");

        SimpleListItemBean item = null;
        //first tr is title, skip
        Log.e(LOG_TAG, "tr.size=" + trES.size());
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

            item.setId(tid);
            item.setTitle(title);
            item.setTime(time);

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
            item.setTitle(item.getAuthor());
            Elements uidAES = citeES.first().select("a");
            if (uidAES.size() == 0) {
                continue;
            }
            String uid = uidAES.first().attr("href");
            item.setId(HttpUtils.getMiddleString(uid, "uid=", "&"));

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
            item.setInfo(summaryES.first().text());

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

        for (Node n : root.childNodes()) {
            if (n.nodeName().equals("a")) {
                String href = n.attr("href");
                if (href.startsWith("space.php")) {
                    // user
                    info += (((Element) n).text() + " ");
                    continue;
                }
            }
        }

        Elements aES = root.select("a");
        for (Element a : aES) {
            if (a.attr("href").startsWith("http://www.hi-pda.com/forum/redirect.php?from=notice&goto=findpost")) {
                // Thread Name and TID and PID
                item.setTitle(a.text());
                item.setId(HttpUtils.getMiddleString(a.attr("href"), "ptid=", ""));
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

        info += ("回复了您的帖子 ");

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
        String info = "";

        Elements aES = root.select("a");
        for (Element a : aES) {
            if (a.attr("href").startsWith("http://www.hi-pda.com/forum/viewthread.php")) {
                // Thread Name and TID and PID
                item.setTitle(a.text());
                continue;
            }
            if (a.attr("href").startsWith("http://www.hi-pda.com/forum/redirect.php?from=notice&goto=findpost")) {
                // Thread Name and TID and PID
                item.setId(HttpUtils.getMiddleString(a.attr("href"), "ptid=", ""));
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

        // summary
        Elements summaryES = root.select(".summary");
        if (summaryES.size() > 0) {
            info = summaryES.first().text();
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
                Elements avatarImgES = avatarES.first().select("img");
                if (avatarImgES.size() > 0) {
                    item.setAvatarUrl(avatarImgES.first().attr("src"));
                }
                if (item.getAuthor().equals(myUsername)) {
                    item.setId(myUid);
                } else {
                    String spaceUrl = Utils.nullToText(avatarES.first().attr("href"));
                    item.setId(HttpUtils.getMiddleString(spaceUrl, "uid=", "&"));
                }
            }

            Log.e("XXX", item.getId() + " -- " + item.getAuthor());

            // time
            item.setTime(pciteES.first().ownText());

            // info
            Elements summaryES = smsE.select("div.summary");
            if (summaryES.size() == 0) {
                continue;
            }
            item.setInfo(summaryES.first().text());

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseSearch(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
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
            item.setId(HttpUtils.getMiddleString(href, "tid=", "&"));

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
                item.setTime(timeES.first().text());
            }

            list.add(item);
        }

        return list;
    }

    private static SimpleListBean parseFavorites(Document doc) {
        if (doc == null) {
            return null;
        }

        SimpleListBean list = new SimpleListBean();
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
            item.setId(HttpUtils.getMiddleString(href, "tid=", "&"));

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

        Elements avatarES = doc.select("div.side div.profile_side div.avatar img");
        if (avatarES.size() != 0) {
            info.setmAvatarUrl(avatarES.first().attr("src"));
        }

        Elements detailES = doc.select("div.main div.s_clear ul.commonlist li");
        StringBuilder sb = new StringBuilder();
        for (Element detail : detailES) {
            sb.append(detail.text()).append('\n');
        }

        info.setmDetail(sb.toString());

        return info;
    }
}
