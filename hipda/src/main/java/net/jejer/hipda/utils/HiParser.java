package net.jejer.hipda.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import net.jejer.hipda.async.HiStringRequest;
import net.jejer.hipda.async.SimpleListLoader;
import net.jejer.hipda.async.VolleyHelper;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailBean.Contents;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.SimpleListBean;
import net.jejer.hipda.bean.SimpleListItemBean;
import net.jejer.hipda.bean.ThreadBean;
import net.jejer.hipda.bean.ThreadListBean;
import net.jejer.hipda.bean.UserInfoBean;
import net.jejer.hipda.ui.NotifyHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class HiParser {
	public static final String LOG_TAG = "HiParser";

	public static ThreadListBean parseForumList(Context ctx, Document doc) {
		if (doc == null) {
			return null;
		}
		parseNotify(ctx, doc);

		ThreadListBean threads = new ThreadListBean(ctx);
		Elements tbodyES = doc.select("tbody");
		for (int i = 0; i < tbodyES.size(); ++i) {
			Element tbodyE = tbodyES.get(i);
			String id = tbodyE.attr("id");
			if (id.isEmpty()) {
				continue;
			}

			ThreadBean thread = new ThreadBean();

			/* title and tid */
			String[] idSpil = id.split("_");
			if (idSpil.length != 2) {
				continue;
			}
			String idType = idSpil[0];
			String idNum = idSpil[1];
			String idThread = "thread_" + idNum;
			thread.setTid(idNum);

			Elements titleES = tbodyE.select("#" + idThread);
			if (titleES.size() != 1) {
				continue;
			}
			String title = titleES.first().text();
			thread.setTitle(title);

			/*  author, authorId and create_time  */
			Elements authorES = tbodyE.select(".author");
			if (authorES.size() != 1) {
				continue;
			}
			Elements authorciteES = authorES.first().select("cite");
			if (authorciteES.size() != 1) {
				continue;
			}
			String author = authorciteES.first().text();
			thread.setAuthor(author);

			Elements userlinkES = authorES.select("a");
			if (userlinkES.size() != 1) {
				continue;
			}
			String userLink = userlinkES.first().attr("href");
			if (userLink.length() < "space.php?uid=".length()) {
				continue;
			}
			String authorId = userLink.substring("space.php?uid=".length());
			thread.setAuthorId(authorId);

			Elements threadCreateTimeES = authorES.first().select("em");
			if (threadCreateTimeES.size() != 1) {
				continue;
			}
			String threadCreateTime = threadCreateTimeES.first().text();
			thread.setTimeCreate(threadCreateTime);

			/*  comments and views  */
			Elements nums = tbodyE.select(".nums");
			if (nums.size() != 1) {
				continue;
			}
			Elements comentsES = nums.first().select("strong");
			if (comentsES.size() != 1) {
				continue;
			}
			String comments = comentsES.first().text();
			thread.setCountCmts(comments);

			Elements viewsES = nums.first().select("em");
			if (viewsES.size() != 1) {
				continue;
			}
			String views = viewsES.first().text();
			thread.setCountViews(views);

			// lastpost
			Elements lastpostES = tbodyE.select("td.lastpost");
			if (lastpostES.size() != 1) {
				continue;
			}
			Elements lastpostESciteES = lastpostES.first().select("cite");
			if (lastpostESciteES.size() != 1) {
				continue;
			}
			String lastpost = lastpostESciteES.first().text();
			thread.setLastPost(lastpost);

			// is stick thread or normal thread
			Boolean isStick = idType.startsWith("stickthread");
			thread.setIsStick(isStick);

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

	public static DetailListBean parseDetail(Context ctx, Document doc) {
		if (doc == null) {
			return null;
		}
		parseNotify(ctx, doc);

		DetailListBean details = new DetailListBean();

		// if next page exist
		Elements nextES = doc.select("a.next");
		if (nextES.size() < 1) {
			details.setHaveNext(false);
		} else {
			details.setHaveNext(true);
		}

		// get last page
		Elements pagesES = doc.select("div.pages");
		// thread have only 1 page don't have "div.pages"
		int last_page = 1;
		if (pagesES.size() != 0) {
			for (Node n : pagesES.first().childNodes()) {
				int tmp = HttpUtils.getIntFromString(((Element) n).text());
				if (tmp > last_page) {
					last_page = tmp;
				}
			}
		}
		details.setLastPage(last_page);
		//Log.v("TEST_LAST_PAGE", String.valueOf(last_page));

		Elements rootES = doc.select("#postlist");
		if (rootES.size() != 1) {
			return null;
		}
		Element root = rootES.first();

		for (int i = 0; i < root.childNodeSize(); i++) {
			//for (int i = 0; i < 1; i++) {
			Element postE = root.child(i);

			DetailBean detail = new DetailBean();

			//id
			String id = postE.attr("id");
			if (id.length() < "post_".length()) {
				continue;
			}
			id = id.substring("post_".length());
			detail.setPostId(id);

			//author
			Elements postauthorES = postE.select(".postauthor");
			if (postauthorES.size() < 1) {
				continue;
			}
			Elements postauthorAES = postauthorES.first().select("a");
			if (postauthorAES.size() < 1) {
				continue;
			}
			String author = postauthorAES.first().text();
			detail.setAuthor(author);

			//time
			Elements authorinfoES = postE.select("div.authorinfo");
			if (authorinfoES.size() < 1) {
				continue;
			}
			Elements timeEMES = authorinfoES.first().select("em");
			if (timeEMES.size() < 1) {
				continue;
			}
			String time = timeEMES.first().text();
			detail.setTimePost(time);

			//floor
			String floor = "";
			Elements postinfoES = postE.select("div.postinfo");
			if (postinfoES.size() < 2) {
				continue;
			}
			Elements postinfoAES = postinfoES.get(1).select("a");
			if (postinfoAES.size() < 1) {
				continue;
			}
			floor = postinfoAES.first().text();
			detail.setFloor(floor);

			//avatar
			Elements avatarES = postE.select("div.avatar");
			if (avatarES.size() < 1) {
				// avatar display can be closed by user
				detail.setAvatarUrl("noavatar");
			} else {
				Elements avatarimgES = avatarES.first().select("img");
				if (avatarimgES.size() < 1) {
					continue;
				}
				detail.setAvatarUrl(avatarimgES.first().attr("src"));
			}


			//content
			//Elements postmessageES = postE.select("#postmessage_"+id);
			Elements postmessageES = postE.select("div.t_msgfontfix");
			if (postmessageES.size() < 1) {
				continue;
			}
			Element postmessageE = postmessageES.first();

			//post status
			Elements poststatusES = postmessageE.select(".pstatus");
			if (poststatusES.size() > 0) {
				String poststatus = poststatusES.first().text();
				detail.setPostStatus(poststatus);
				//remove then it will not show in content
				poststatusES.first().remove();
			}

			Contents content = detail.getContents();
			Node contentN = postmessageE.childNode(0);
			int level = 1;
			while (level > 0 && contentN != null) {
				parseNode(contentN, content);

				if (contentN.childNodeSize() > 0) {
					contentN = contentN.childNode(0);
					level++;
					continue;
				}

				if (contentN.nextSibling() != null) {
					contentN = contentN.nextSibling();
					continue;
				} else {
					while (contentN.parent().nextSibling() == null) {
						contentN = contentN.parent();
						level--;
					}
					contentN = contentN.parent().nextSibling();
					level--;
					continue;
				}
			}

			details.add(detail);
		}
		return details;
	}

	private static void parseNode(Node contentN, DetailBean.Contents content) {
		//Log.v(LOG_TAG, contentN.nodeName());

		if (contentN.nodeName().equals("font")    // textfont
				|| contentN.nodeName().equals("i")    //text in an alternate voice or mood
				|| contentN.nodeName().equals("u")    //text that should be stylistically different from normal text
				|| contentN.nodeName().equals("em")    //text emphasized
				|| contentN.nodeName().equals("strike")    //text strikethrough
				|| contentN.nodeName().equals("ol")    //ordered list
				|| contentN.nodeName().equals("ul")    //unordered list
				|| contentN.nodeName().equals("hr")) {    //a thematic change in the content(h line)
			//continue parse child node
			return;
		} else if (contentN.nodeName().equals("strong")) {
			String tmp = ((Element) contentN).text();
			if (tmp.startsWith("回复 ") && tmp.length() < (3 + 6 + 15) && tmp.contains("#")) {
				int floor = HttpUtils.getIntFromString(tmp.substring(0, tmp.indexOf("#")));
				if (floor > 0) {
					content.addGoToFloor(tmp, floor);
					removeAllChild(contentN);
				}
			}
			return;
		} else if (contentN.nodeName().equals("#text")) {
			//Log.v(LOG_TAG, contentN.toString());
			String text = contentN.toString();
			if (isHaveText(text)) {
				content.addText(text);
			}
			return;
		} else if (contentN.nodeName().equals("li")) {    // list item
			content.addText("\n");
			return;
		} else if (contentN.nodeName().equals("br")) {    // single line break
			content.addText("\n");
			return;
		} else if (contentN.nodeName().equals("p")) {    // paragraph
			Element pE = (Element) contentN;
			if (pE.hasClass("imgtitle")) {
				//Remove imgtitle
				removeAllChild(contentN);
				return;
			}
			return;
		} else if (contentN.nodeName().equals("img")) {
			Element e = (Element) contentN;
			String src = e.attr("src");

			if (src.startsWith("images/smilies/")) {
				//emotion
				content.addText("[emoticon " + src + "]");
				return;
			} else if (src.equals("images/common/none.gif") || src.startsWith("attachments/day_")) {
				//internal image
				content.addImg(e.attr("file"), true);
				removeAllChild(contentN);
				return;
			} else if (src.equals("images/common/")) {
				//skip common icons
				return;
			} else if (src.startsWith("http://") || src.startsWith("https://")) {
				//external image
				content.addImg(src, false);
				removeAllChild(contentN);
				return;
			} else if (src.startsWith("images/attachicons/")) {
				//attach icon
				removeAllChild(contentN);
				return;
			} else if (src.startsWith("images/default/")) {
				//default icon
				removeAllChild(contentN);
				return;
			} else {
				//
				content.addText("[[ERROR:UNPARSED IMG:" + src + "]]");
				Log.e(LOG_TAG, "[[ERROR:UNPARSED IMG:" + src + "]]");
				return;
			}
		} else if (contentN.nodeName().equals("span")) {    // a section in a document
			Elements attachAES = ((Element) contentN).select("a");
			Boolean isInternalAttach = false;
			for (int attIdx = 0; attIdx < attachAES.size(); attIdx++) {
				Element attachAE = attachAES.get(attIdx);
				if (attachAE.attr("href").startsWith("attachment.php?")) {
					content.addAttach(attachAE.attr("href"), attachAE.text());
					isInternalAttach = true;
				}
			}
			if (isInternalAttach) {
				removeAllChild(contentN);
			}
			return;
		} else if (contentN.nodeName().equals("a")) {
			Element aE = (Element) contentN;
			String text = aE.text();
			String url = aE.attr("href");
			if (url.startsWith("attachment.php?")) {
				// is Attachment
				content.addAttach(url, text);
				removeAllChild(contentN);
				return;
			}

			if (text.startsWith("http://") || text.startsWith("https://")) {
				text = "";
			} else {
				text = text + " ";
			}
			if (url.startsWith("http://www.hi-pda.com/forum/redirect.php?goto=")) {
				// goto floor url
				url = "";
			}
			content.addText("[" + text + url + "]");
			removeAllChild(contentN);
			return;
		} else if (contentN.nodeName().equals("div")) {    // a section in a document
			Element divE = (Element) contentN;
			if (divE.hasClass("t_attach")) {
				// remove div.t_attach
				removeAllChild(contentN);
				return;
			} else if (divE.hasClass("quote")) {
				content.addQuote(divE.text());
				removeAllChild(contentN);
				return;
			} else if (divE.hasClass("attach_popup")) {
				// remove div.attach_popup
				removeAllChild(contentN);
				return;
			}
			return;
		} else if (contentN.nodeName().equals("table")) {
			return;
		} else if (contentN.nodeName().equals("tbody")) {    //Groups the body content in a table
			return;
		} else if (contentN.nodeName().equals("tr")) {    //a row in a table
			content.addText("\n");
			return;
		} else if (contentN.nodeName().equals("td")) {    //a cell in a table
			content.addText(" ");
			return;
		} else if (contentN.nodeName().equals("dl")) {    //a description list
			return;
		} else if (contentN.nodeName().equals("dt")) {    //a term/name in a description list
			return;
		} else if (contentN.nodeName().equals("dd")) {    //a description/value of a term in a description list
			return;
		} else if (contentN.nodeName().equals("script") || contentN.nodeName().equals("#data")) {
			// video
			String html = contentN.toString();
			String url = HttpUtils.getMiddleString(html, "'src', '", "'");
			if (url != null && url.startsWith("http")) {
				content.addText("[FLASH VIDEO,手机可能不支持 " + url + "]");
			}
			removeAllChild(contentN);
			return;
		} else {
			content.addText("[[ERROR:UNPARSED TAG:" + contentN.nodeName() + ":" + contentN.toString() + "]]");
			Log.e(LOG_TAG, "[[ERROR:UNPARSED TAG:" + contentN.nodeName() + "]]");
			return;
		}
	}

	private static void removeAllChild(Node n) {
		while (n.childNodeSize() > 0) {
			n.childNode(0).remove();
		}
	}

	private static Boolean isHaveText(String str) {
		return !str.equals(" ");
	}

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

		Elements smslistES = doc.select("li.s_clear");
		if (smslistES.size() < 1) {
			return null;
		}

		SimpleListBean list = new SimpleListBean();
		for (int i = 0; i < smslistES.size(); ++i) {
			Element smsE = smslistES.get(i);
			SimpleListItemBean item = new SimpleListItemBean();

			// avatar
			Elements avatarES = smsE.select("a.avatar");
			if (avatarES.size() > 0) {
				Elements avatarImgES = avatarES.first().select("img");
				if (avatarImgES.size() > 0) {
					item.setAvatarUrl(avatarImgES.first().attr("src"));
				}
			}

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

	public static void parseNotify(Context ctx, Document doc) {
		Elements promptcontentES = doc.select("div.promptcontent");
		if (promptcontentES.size() < 1) {
			return;
		}

		String notifyStr = promptcontentES.first().text();
		//私人消息 (1) 公共消息 (0) 系统消息 (0) 好友消息 (0) 帖子消息 (0)
		int cnt = 0;
		for (String s : notifyStr.split("\\) ")) {
			cnt = 0;
			if (s.contains("私人消息")) {
				cnt = HttpUtils.getIntFromString(s);
				NotifyHelper.getInstance().setCntSMS(cnt);
				Log.v("NEW SMS:", String.valueOf(cnt));
				continue;
			} else if (s.contains("帖子消息")) {
				cnt = HttpUtils.getIntFromString(s);
				NotifyHelper.getInstance().setCntThread(cnt);
				Log.v("THREAD NOTIFY:", String.valueOf(cnt));
				continue;
			}
		}

		// Trigger Refresh SMS, result will show in next load.
		StringRequest sReq = new HiStringRequest(ctx, HiUtils.CheckSMS,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						// TODO Auto-generated method stub
					}
				}, null);
		VolleyHelper.getInstance().add(sReq);
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
