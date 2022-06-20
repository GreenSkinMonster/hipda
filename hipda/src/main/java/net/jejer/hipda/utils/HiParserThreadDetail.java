package net.jejer.hipda.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.vdurmont.emoji.EmojiParser;

import net.jejer.hipda.bean.ContentImg;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailBean.Contents;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.HiSettingsHelper;
import net.jejer.hipda.bean.PollBean;
import net.jejer.hipda.bean.PollOptionBean;
import net.jejer.hipda.cache.SignatureContainer;
import net.jejer.hipda.cache.SmallImages;
import net.jejer.hipda.ui.textstyle.TextStyle;
import net.jejer.hipda.ui.textstyle.TextStyleHolder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class HiParserThreadDetail {

    public static String getThreadAuthorId(Document doc) {
        Elements authorLinksEs = doc.select("td.postauthor div.postinfo a");
        if (authorLinksEs.size() > 0) {
            String uidUrl = authorLinksEs.first().attr("href");
            return Utils.getMiddleString(uidUrl, "uid=", "&");
        }
        return "";
    }

    public static DetailListBean parse(Context context, Document doc, String tid) {
        // get last page
        Elements pagesES = doc.select("div#wrap div.forumcontrol div.pages");
        // thread have only 1 page don't have "div.pages"
        int last_page = 1;
        int page = 1;
        if (pagesES.size() != 0) {
            for (Node n : pagesES.first().childNodes()) {
                int tmp = Utils.getIntFromString(((Element) n).text());
                if (tmp > last_page) {
                    last_page = tmp;
                }
                if ("strong".equals(n.nodeName())) {
                    page = tmp;
                }
            }
        }

        new Thread(new HiParserThreadList.ParseNotifyRunnable(context, doc)).start();
        HiSettingsHelper.updateMobileNetworkStatus(context);

        DetailListBean details = new DetailListBean();
        details.setPage(page);
        details.setLastPage(last_page);

        if (!TextUtils.isEmpty(tid) && TextUtils.isDigitsOnly(tid))
            details.setTid(tid);

        //get forum id
        Elements divNavES = doc.select("div#nav");
        if (divNavES.size() > 0) {
            Elements divNavLinkES = divNavES.first().select("a");
            if (divNavLinkES.size() > 0) {
                for (int i = 0; i < divNavLinkES.size(); i++) {
                    Element forumLink = divNavLinkES.get(i);
                    String forumUrl = Utils.nullToText(forumLink.attr("href"));
                    if (forumUrl.indexOf("fid=") > 0) {
                        details.setFid(Utils.parseInt(Utils.getMiddleString(forumUrl, "fid=", "&")));
                        break;
                    }
                }
            }
            //get thread title from nav div
            divNavLinkES.remove();
            String title = divNavES.text();
            title = title.replace("»", "").trim();
            details.setTitle(EmojiParser.parseToUnicode(title));
        }

        //Title, only avaliable in first page
        if (TextUtils.isEmpty(details.getTitle())) {
            Elements threadtitleES = doc.select("div#threadtitle");
            if (threadtitleES.size() > 0) {
                threadtitleES.select("a").remove();
                details.setTitle(threadtitleES.first().text());
            }
        }

        Elements rootES = doc.select("div#wrap div#postlist");
        if (rootES.size() != 1) {
            return null;
        }
        Element postsEL = rootES.first();
        for (int i = 0; i < postsEL.childNodeSize(); i++) {
            Element postE = postsEL.child(i);

            DetailBean detail = new DetailBean();
            detail.setPage(page);

            //id
            String id = postE.attr("id");
            if (id.length() < "post_".length()) {
                continue;
            }
            id = id.substring("post_".length());
            detail.setPostId(id);

            //time
            Elements timeEMES = postE.select("table tbody tr td.postcontent div.postinfo div.posterinfo div.authorinfo em");
            if (timeEMES.size() == 0) {
                continue;
            }
            String time = timeEMES.first().text();
            detail.setTimePost(time);

            //floor
            Elements postinfoAES = postE.select("table tbody tr td.postcontent div.postinfo strong a em");
            if (postinfoAES.size() == 0) {
                continue;
            }
            String floor = postinfoAES.first().text();
            detail.setFloor(Utils.parseInt(floor));

            //warning
            Elements warningES = postE.select("table tbody tr td.postcontent span.postratings a");
            if (warningES.size() > 0 && warningES.first().attr("href").contains("viewwarning")) {
                detail.setWarned(true);
            }

            //update max posts in page, this is controlled by user setting
            if (i == 0) {
                if (page == 1 && last_page > 1) {
                    HiSettingsHelper.getInstance().setMaxPostsInPage(postsEL.childNodeSize());
                } else if (page > 1) {
                    int maxPostsInPage = (Utils.parseInt(floor) - 1) / (page - 1);
                    HiSettingsHelper.getInstance().setMaxPostsInPage(maxPostsInPage);
                }
            }

            //author
            Elements postauthorAES = postE.select("table tbody tr td.postauthor div.postinfo a");
            if (postauthorAES.size() == 0) {
                continue;
            }
            String uidUrl = postauthorAES.first().attr("href");
            String uid = Utils.getMiddleString(uidUrl, "uid=", "&");
            if (!TextUtils.isEmpty(uid)) {
                detail.setUid(uid);
            } else {
                continue;
            }

            String author = postauthorAES.first().text();
            if (!detail.setAuthor(author)) {
                detail.setAuthor("[[黑名单用户]]");
                details.add(detail);
                continue;
            }

            //avatar
//            Elements avatarES = postE.select("table tbody tr td.postauthor div div.avatar a img");
//            if (avatarES.size() == 0) {
//                // avatar display can be closed by user
//                detail.setAvatarUrl("noavatar");
//            } else {
//                detail.setAvatarUrl(avatarES.first().attr("src"));
//            }
            detail.setAvatarUrl(HiUtils.getAvatarUrlByUid(uid));

            Element sigEl = postE.select("td.postbottom > div.signatures").first();
            if (sigEl != null) {
                SignatureContainer.putSignature(uid, sigEl.text());
            }

            //content
            Contents content = detail.getContents();
            Elements postmessageES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.t_msgfontfix table tbody tr td.t_msgfont");

            //locked user content
            if (postmessageES.size() == 0) {
                postmessageES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.locked");
                if (postmessageES.size() > 0) {
                    content.addNotice(postmessageES.text());
                    details.add(detail);
                    continue;
                }
            }

            //投票
            try {
                if ("1".equals(floor)) {
                    Element pollEl = doc.select("form#poll").first();
                    if (pollEl != null) {
                        PollBean pollBean = new PollBean();
                        Element pollInfoEl = pollEl.select("div.pollinfo").first();
                        pollInfoEl.select("a").remove();
                        pollBean.setTitle(pollInfoEl.html());
                        Element formhashEl = pollEl.select("input[name=formhash]").first();
                        if (formhashEl != null)
                            pollBean.setFormhash(formhashEl.val());
                        Element pollTimerEl = pollEl.select("p.polltimer").first();
                        if (pollTimerEl != null) {
                            pollBean.setTitle(pollBean.getTitle() + "<br>" + pollTimerEl.html());
                        }
                        if (pollInfoEl.text().trim().startsWith("多选投票")) {
                            int maxAnswer = Utils.parseInt(Utils.getMiddleString(pollInfoEl.text(), "最多可选", "项"));
                            if (maxAnswer > 1)
                                pollBean.setMaxAnswer(maxAnswer);
                        }

                        List<PollOptionBean> options = new ArrayList<>();
                        Elements optionsES = pollEl.select("div.pollchart > table > tbody > tr");
                        for (Element optionEl : optionsES) {
                            Element checkEl = optionEl.select("td.selector input").first();
                            Element textEl = optionEl.select("td.polloption label").first();
                            if (textEl != null) {
                                PollOptionBean option = new PollOptionBean();
                                if (checkEl != null)
                                    option.setOptionId(checkEl.attr("value"));
                                option.setText(textEl.text());
                                options.add(option);
                            } else {
                                Element ratesTdEl = optionEl.select("td").last();
                                if (ratesTdEl != null && options.size() > 0 && ratesTdEl.select("em").size() > 0) {
                                    options.get(options.size() - 1).setRates(ratesTdEl.text());
                                }
                            }
                        }
                        Element lastTr = pollEl.select("div.pollchart > table > tbody > tr").last();
                        if (lastTr != null && lastTr.select("td").size() >= 1) {
                            Element lastTd = lastTr.select("td").last();
                            if (lastTd != null) {
                                lastTd.select("button").remove();
                                pollBean.setFooter(lastTd.text());
                            }
                        }

                        pollBean.setPollOptions(options);
                        detail.setPoll(pollBean);
                        pollEl.remove();
                    }
                }
            } catch (Exception e) {
                Logger.e(e);
            }

            //poll content
            boolean isPollFirstPost = false;
            if (postmessageES.size() == 0) {
                postmessageES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.specialmsg table tbody tr td.t_msgfont");
                isPollFirstPost = "1".equals(floor);
            }
            if (isPollFirstPost) {
                StringBuilder sb = new StringBuilder();
                sb.append(postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.pollinfo").text()).append("<br>");
                Elements pollOptions = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.pollchart table  tbody tr");
                for (int j = 0; j < pollOptions.size(); j++) {
                    if (j % 2 == 0 && j < pollOptions.size() - 1)
                        sb.append(pollOptions.get(j).text());
                    if (j % 2 == 1)
                        sb.append(pollOptions.get(j).text()).append("<br>");
                }
                sb.append("<br>");
                content.addText(sb.toString());
            }

            if (postmessageES.size() == 0) {
                content.addNotice("[[!!找不到帖子内容，可能是该帖被管理员或版主屏蔽!!]]");
                details.add(detail);
                continue;
            }

            Element postmessageE = postmessageES.first();
            if (postmessageE.childNodeSize() == 0) {
                content.addNotice("[[无内容]]");
                details.add(detail);
                continue;
            }

            //post status
            Elements poststatusES = postmessageE.select("i.pstatus");
            if (poststatusES.size() > 0) {
                String poststatus = poststatusES.first().text();
                detail.setPostStatus(poststatus);
                //remove then it will not show in content
                poststatusES.first().remove();
            }

            // Nodes including Elements(have tag) and text without tag
            TextStyleHolder textStyles = new TextStyleHolder();
            Node contentN = postmessageE.childNode(0);
            int level = 1;
            boolean processChildren;
            while (level > 0 && contentN != null) {

                textStyles.addLevel(level);

                processChildren = parseNode(contentN, content, level, textStyles);

                if (processChildren && contentN.childNodeSize() > 0) {
                    contentN = contentN.childNode(0);
                    level++;
                } else if (contentN.nextSibling() != null) {
                    contentN = contentN.nextSibling();
                    textStyles.removeLevel(level);
                } else {
                    while (contentN.parent().nextSibling() == null) {
                        contentN = contentN.parent();
                        textStyles.removeLevel(level);
                        textStyles.removeLevel(level - 1);
                        level--;
                    }
                    contentN = contentN.parent().nextSibling();
                    textStyles.removeLevel(level);
                    textStyles.removeLevel(level - 1);
                    level--;
                }
            }

            // IMG attachments
            Elements dlES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.t_msgfontfix div.postattachlist dl.attachimg");
            for (int j = 0; j < dlES.size(); j++) {
                Element dlEl = dlES.get(j);
                Elements sizeES = dlEl.select("em");
                Elements imgES = dlEl.select("img");

                long size = 0;
                if (sizeES.size() > 0) {
                    String sizeText = Utils.getMiddleString(sizeES.first().text(), "(", ")");
                    size = Utils.parseSizeText(sizeText);
                }

                if (imgES.size() > 0) {
                    Element e = imgES.first();
                    ContentImg contentImg = getContentImg(e, size);
                    content.addImg(contentImg);
                }
            }

            // other attachments
            Elements attachmentES = postE.select("dl.t_attachlist p.attachname");
            for (int j = 0; j < attachmentES.size(); j++) {
                Element attachE = attachmentES.get(j);
                Elements attachLinkES = attachE.select("a[href]");

                if (attachLinkES.size() > 0) {
                    Element linkE = attachLinkES.first();
                    if (linkE.attr("href").startsWith("attachment.php?")) {
                        attachLinkES.remove();
                        String desc = attachE.text();

                        if (j == 0)
                            content.addText("<br>");
                        content.addAttach(linkE.attr("href"), linkE.text(), desc);
                    }
                }
            }

            details.add(detail);
        }
        return details;
    }

    // return true for continue children, false for ignore children
    private static boolean parseNode(Node contentN, DetailBean.Contents content, int level, @NonNull TextStyleHolder textStyles) {

        if (contentN.nodeName().equals("i")    //text in an alternate voice or mood
                || contentN.nodeName().equals("u")    //text that should be stylistically different from normal text
                || contentN.nodeName().equals("em")    //text emphasized
                || contentN.nodeName().equals("strike")    //text strikethrough
                || contentN.nodeName().equals("ol")    //ordered list
                || contentN.nodeName().equals("ul")    //unordered list
                || contentN.nodeName().equals("hr")   //a thematic change in the content(h line)
                || contentN.nodeName().equals("blockquote")
                || contentN.nodeName().equals("font")) {
            textStyles.addStyle(level, contentN.nodeName());
            if (contentN.nodeName().equals("font")) {
                Element elemFont = (Element) contentN;
                if (elemFont.attr("size").equals("1"))
                    textStyles.setSmallFont(level, true);
                textStyles.setColor(level, Utils.nullToText(elemFont.attr("color")).trim());
            }
            //continue parse child node
            return true;
        } else if (contentN.nodeName().equals("strong")) {
            String tmp = ((Element) contentN).text();
            String postId = "";
            String tid = "";
            Elements floorLink = ((Element) contentN).select("a[href]");
            if (floorLink.size() > 0) {
                postId = Utils.getMiddleString(floorLink.first().attr("href"), "pid=", "&");
                tid = Utils.getMiddleString(floorLink.first().attr("href"), "ptid=", "&");
            }
            if (tmp.startsWith("回复 ") && tmp.contains("#")) {
                int floor = Utils.getIntFromString(tmp.substring(0, tmp.indexOf("#")));
                String author = tmp.substring(tmp.lastIndexOf("#") + 1).trim();
                if (!TextUtils.isEmpty(author) && HiUtils.isValidId(postId) && floor > 0) {
                    content.addGoToFloor(tmp, tid, postId, floor, author);
                    return false;
                }
            }
            textStyles.addStyle(level, contentN.nodeName());
            return true;
        } else if (contentN.nodeName().equals("#text")) {
            //replace  < >  to &lt; &gt; , or they will become to unsupported tag
            String text = ((TextNode) contentN).text();
            if (TextUtils.isEmpty(text))
                return false;

            text = text.replace("<", "&lt;")
                    .replace(">", "&gt;");

            TextStyle ts = null;
            if (textStyles.getTextStyle(level - 1) != null)
                ts = textStyles.getTextStyle(level - 1).newInstance();

            Matcher matcher = Utils.URL_PATTERN.matcher(text);

            int lastPos = 0;
            while (matcher.find()) {
                String t = text.substring(lastPos, matcher.start());
                String url = text.substring(matcher.start(), matcher.end());

                if (!TextUtils.isEmpty(t.trim())) {
                    content.addText(t, ts);
                }
                if (url.contains("@") && !url.contains("/")) {
                    content.addEmail(url);
                } else {
                    content.addLink(url, url, ts != null && ts.isSmallFont());
                }
                lastPos = matcher.end();
            }
            if (lastPos < text.length()) {
                String t = text.substring(lastPos);
                if (!TextUtils.isEmpty(t.trim())) {
                    content.addText(t, ts);
                }
            }
            return false;
        } else if (contentN.nodeName().equals("li")) {    // list item
            return true;
        } else if (contentN.nodeName().equals("br")) {    // single line break
            content.addText("<br>");
            return false;
        } else if (contentN.nodeName().equals("p")) {    // paragraph
            Element pE = (Element) contentN;
            if (pE.hasClass("imgtitle")) {
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("img")) {
            parseImageElement((Element) contentN, content);
            return false;
        } else if (contentN.nodeName().equals("span")) {    // a section in a document
            Elements attachAES = ((Element) contentN).select("a");
            boolean isInternalAttach = false;
            for (int attIdx = 0; attIdx < attachAES.size(); attIdx++) {
                Element attachAE = attachAES.get(attIdx);
                //it is an attachment and not an image attachment
                if (attachAE.attr("href").contains("attachment.php?")
                        && !attachAE.attr("href").contains("nothumb=")) {
                    String desc = "";
                    Node sibNode = contentN.nextSibling();
                    if (sibNode != null && sibNode.nodeName().equals("#text")) {
                        desc = sibNode.toString();
                        sibNode.remove();
                    }
                    content.addAttach(attachAE.attr("href"), attachAE.text(), desc);
                    isInternalAttach = true;
                }
            }
            if (isInternalAttach) {
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("a")) {
            Element aE = (Element) contentN;
            String text = aE.text();
            String url = aE.attr("href");
            if (aE.childNodeSize() > 0 && aE.childNode(0).nodeName().equals("img")) {
                if (!url.startsWith("javascript:"))
                    content.addLink(url, url, false);
                return true;
            }

            if (url.startsWith("attachment.php?")) {
                content.addAttach(url, text, null);
                return false;
            }

            //处理小尾巴链接字体大小
            boolean smallFont = false;
            TextStyle ts = textStyles.getTextStyle(level - 1);
            if (ts != null && ts.isSmallFont())
                smallFont = true;
            if (!smallFont && aE.childNodeSize() > 0 && aE.childNode(0).nodeName().equals("font")) {
                if (aE.childNode(0).attr("size").equals("1"))
                    smallFont = true;
            }
            if (smallFont)
                url = HiUtils.replaceOldDomain(url);
            content.addLink(text, url, smallFont);
            //rare case, link tag contains images
            Elements imgEs = aE.select("img");
            if (imgEs.size() > 0) {
                for (int i = 0; i < imgEs.size(); i++) {
                    parseImageElement(imgEs.get(i), content);
                }
            }
            return false;
        } else if (contentN.nodeName().equals("div")) {    // a section in a document
            Element divE = (Element) contentN;
            if (divE.hasClass("t_attach")) {
                // remove div.t_attach
                return false;
            } else if (divE.hasClass("quote")) {
                String tid = "";
                String postId = "";
                Elements redirectES = divE.select("a");
                for (Element element : redirectES) {
                    String href = Utils.nullToText(element.attr("href"));
                    if (href.contains("redirect.php?goto=findpost")) {
                        postId = Utils.getMiddleString(href, "pid=", "&");
                        tid = Utils.getMiddleString(href, "ptid=", "&");
                        break;
                    }
                }
                Elements postEls = divE.select("font[size=2]");
                String authorAndTime = "";
                if (postEls.size() > 0) {
                    authorAndTime = postEls.first().text();
                    postEls.first().remove();
                }

                //remove hidden elements
                divE.select("[style*=display][style*=none]").remove();

                //only keep line break, text with styles, links
                content.addQuote(Utils.clean(divE.html()), authorAndTime, tid, postId);
                return false;
            } else if (divE.hasClass("attach_popup")) {
                // remove div.attach_popup
                return false;
            }
            return true;
        } else if (contentN.nodeName().equals("table")) {
            return true;
        } else if (contentN.nodeName().equals("tbody")) {    //Groups the body content in a table
            return true;
        } else if (contentN.nodeName().equals("tr")) {    //a row in a table
            content.addText("<br>");
            return true;
        } else if (contentN.nodeName().equals("td")) {    //a cell in a table
            content.addText(" ");
            return true;
        } else if (contentN.nodeName().equals("dl")) {    //a description list
            return true;
        } else if (contentN.nodeName().equals("dt")) {    //a term/name in a description list
            return true;
        } else if (contentN.nodeName().equals("dd")) {    //a description/value of a term in a description list
            return true;
        } else if (contentN.nodeName().equals("script") || contentN.nodeName().equals("#data")) {
            // video
            String html = contentN.toString();
            String url = Utils.getMiddleString(html, "'src', '", "'");
            if (url.startsWith("http://player.youku.com/player.php")) {
                //http://player.youku.com/player.php/sid/XNzIyMTUxMzEy.html/v.swf
                //http://v.youku.com/v_show/id_XNzIyMTUxMzEy.html
                url = Utils.getMiddleString(url, "sid/", "/v.swf");
                url = "http://v.youku.com/v_show/id_" + url;
                if (!url.endsWith(".html")) {
                    url = url + ".html";
                }
                content.addLink("YouKu视频自动转换手机通道 " + url, url, false);
            } else if (url.startsWith("http")) {
                content.addLink("FLASH VIDEO,手机可能不支持 " + url, url, false);
            }
            return false;
        } else {
            if (HiSettingsHelper.getInstance().isErrorReportMode()
                    && !"#comment".equals(contentN.nodeName())) {
                content.addNotice("[[ERROR:UNPARSED TAG:" + contentN.nodeName() + ":" + contentN.toString() + "]]");
                Logger.e("[[ERROR:UNPARSED TAG:" + contentN.nodeName() + "]]");
            }
            return false;
        }
    }

    private static void parseImageElement(Element e, Contents content) {
        String src = getAbsoluteUrl(e.attr("src"));
        String id = e.attr("id");

        if (id.startsWith("aimg") || src.contains("images/common/none.gif")) {
            //internal image
            long size = 0;
            Elements divES = (e.parent().parent()).select("div#" + id + "_menu");
            if (divES.size() > 0) {
                String sizeText = Utils.getMiddleString(divES.first().text(), "(", ")");
                size = Utils.parseSizeText(sizeText);
            }

            ContentImg contentImg = getContentImg(e, size);
            content.addImg(contentImg);
        } else if (src.contains(HiUtils.SmiliesPattern)) {
            //emotion added as img tag, will be parsed in TextViewWithEmoticon later
            content.addText("<img src=\"" + src + "\"/>");
        } else if (SmallImages.contains(src)) {
            content.addText("<img src=\"" + src + "\"/>");
        } else if (src.contains(HiUtils.ForumImagePattern)) {
            //skip common/default/attach icons
        } else if (src.contains("data:image/")) {
            //skip base64 images
        } else if (src.contains("://")) {
            //external image
            content.addImg(src);
        } else {
            content.addNotice("[[ERROR:UNPARSED IMG:" + src + "]]");
        }
    }

    @NonNull
    private static ContentImg getContentImg(Element e, long size) {
        String src = getAbsoluteUrl(e.attr("src"));
        String file = getAbsoluteUrl(e.attr("file"));
        String onclick = e.attr("onclick");

        if (onclick.startsWith("zoom") && onclick.contains("attachment")) {
            onclick = "attachment" + Utils.getMiddleString(onclick, "attachment", "'");
        } else {
            onclick = "";
        }
        onclick = getAbsoluteUrl(onclick);

        String thumbUrl = "";
        if (!TextUtils.isEmpty(src) && src.contains("thumb.")) {
            thumbUrl = src;
        }
        String fullUrl = TextUtils.isEmpty(onclick) ? file : onclick;
        if (TextUtils.isEmpty(fullUrl))
            fullUrl = thumbUrl;
        return new ContentImg(fullUrl, size, thumbUrl);
    }

    @NonNull
    private static String getAbsoluteUrl(String url) {
        if (TextUtils.isEmpty(url) || url.contains("://"))
            return url;
        return HiUtils.BaseUrl + url;
    }

}
