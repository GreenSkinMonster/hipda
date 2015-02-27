package net.jejer.hipda.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import net.jejer.hipda.bean.DetailBean;
import net.jejer.hipda.bean.DetailListBean;
import net.jejer.hipda.bean.DetailBean.Contents;
import net.jejer.hipda.ui.ThreadDetailFragment;
import net.jejer.hipda.ui.ThreadListFragment;

public class HiParserThreadDetail {
	public static final String LOG_TAG = "HiParserThreadDetail";

	public static DetailListBean parse(Context ctx, Handler handler, int page, Document doc) {
		// Update UI
		Message msgStartParse = Message.obtain();
		msgStartParse.what = ThreadListFragment.STAGE_PARSE;
		Bundle b = new Bundle();
		b.putInt(ThreadDetailFragment.LOADER_PAGE_KEY, page);
		msgStartParse.setData(b);
		handler.sendMessage(msgStartParse);

		// Async check notify
		new HiParserThreadList.parseNotifyRunnable(ctx, doc).run();

		DetailListBean details = new DetailListBean();
		details.setPage(page);

		// get last page
		Elements pagesES = doc.select("div#wrap div.forumcontrol div.pages");
		// thread have only 1 page don't have "div.pages"
		int last_page = 1;
		if (pagesES.size() != 0) { 
			for (Node n : pagesES.first().childNodes()) {
				int tmp = HttpUtils.getIntFromString(((Element)n).text());
				if (tmp > last_page) { last_page = tmp; }
			}
		}
		details.setLastPage(last_page);
		//Log.v("TEST_LAST_PAGE", String.valueOf(last_page));
		
		//Title
		Elements threadtitleES = doc.select("div#threadtitle");
		if (threadtitleES.size() > 0) {
			details.setTitle(threadtitleES.first().text());
		}

		Elements rootES = doc.select("div#wrap div#postlist");
		if (rootES.size() != 1) { return null; }
		for (int i = 0; i < rootES.first().childNodeSize(); i++) {
			Element postE = rootES.first().child(i);

			DetailBean detail = new DetailBean();

			//id
			String id = postE.attr("id");
			if (id.length() < "post_".length()) { continue; }
			id = id.substring("post_".length());
			detail.setPostId(id);

			//time
			Elements timeEMES = postE.select("table tbody tr td.postcontent div.postinfo div.posterinfo div.authorinfo em");
			if (timeEMES.size() == 0) { continue; }
			String time = timeEMES.first().text();
			detail.setTimePost(time);

			//floor
			Elements postinfoAES = postE.select("table tbody tr td.postcontent div.postinfo strong a em");
			if (postinfoAES.size() == 0) { continue; }
			String floor = postinfoAES.first().text();
			detail.setFloor(floor);

			//author
			Elements postauthorAES = postE.select("table tbody tr td.postauthor div.postinfo a");
			if (postauthorAES.size() == 0) { continue; }
			String uidUrl = postauthorAES.first().attr("href");
			String uid = HttpUtils.getMiddleString(uidUrl, "uid=", "&");
			if (uid != null) {
				detail.setUid(uid);
			} else { continue; }
			
			String author = postauthorAES.first().text();
			if (!detail.setAuthor(author)) {
				detail.setAuthor("[[黑名单用户]]");
				details.add(detail);
				continue;
			}

			//avatar
			Elements avatarES = postE.select("table tbody tr td.postauthor div div.avatar a img");
			if (avatarES.size() == 0) { 
				// avatar display can be closed by user
				detail.setAvatarUrl("noavatar"); 
			} else {
				detail.setAvatarUrl(avatarES.first().attr("src"));
                AvatarHelper.put(uid, detail.getAvatarUrl());
            }

			//content
			Contents content = detail.getContents();
			Elements postmessageES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.t_msgfontfix table tbody tr td.t_msgfont");
			if (postmessageES.size() == 0) { 
				content.addText("[!!找不到帖子内容，可能是该帖被管理员或版主屏蔽!!]");
				details.add(detail);
				continue; 
			}
			Element postmessageE = postmessageES.first();

			//post status
			Elements poststatusES = postmessageE.select("i.pstatus");
			if (poststatusES.size() > 0) {
				String poststatus = poststatusES.first().text();
				detail.setPostStatus(poststatus);
				//remove then it will not show in content
				poststatusES.first().remove();
			}

			// Nodes including Elements(have tag) and text without tag
			Node contentN = postmessageE.childNode(0);
			int level = 1;
			boolean processChildren = true;
			while (level > 0 && contentN != null) {
				processChildren = parseNode(contentN, content);

				if (processChildren && contentN.childNodeSize() > 0) {
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

			// IMG attachments
			Elements postimgES = postE.select("table tbody tr td.postcontent div.defaultpost div.postmessage div.t_msgfontfix div.postattachlist img");
			for (int j = 0; j < postimgES.size(); j++) {
				Element imgE = postimgES.get(j);
				if (imgE.attr("file").startsWith("attachments/day_")) {
					content.addImg(imgE.attr("file"), true);
				}
			}

			details.add(detail);
		}
		return details;
	}

	// return true for continue children, false for ignore children
	private static boolean parseNode(Node contentN, DetailBean.Contents content) {
		//Log.v(LOG_TAG, contentN.nodeName());

		if (contentN.nodeName().equals("font")	// textfont
				|| contentN.nodeName().equals("i")	//text in an alternate voice or mood
				|| contentN.nodeName().equals("u")	//text that should be stylistically different from normal text
				|| contentN.nodeName().equals("em")	//text emphasized 
				|| contentN.nodeName().equals("strike")	//text strikethrough 
				|| contentN.nodeName().equals("ol")	//ordered list
				|| contentN.nodeName().equals("ul")	//unordered list
				|| contentN.nodeName().equals("hr")) {	//a thematic change in the content(h line)
			//continue parse child node
			return true;
		} else if (contentN.nodeName().equals("strong")) {
			String tmp = ((Element)contentN).text();
			if (tmp.startsWith("回复 ") && tmp.length() < (3+6+15) && tmp.contains("#")) {
				int floor = HttpUtils.getIntFromString(tmp.substring(0,  tmp.indexOf("#")));
				if (floor > 0) {
					content.addGoToFloor(tmp, floor);
					return false;
				} 
			}
			return true;
		} else if (contentN.nodeName().equals("#text")) {
			//Log.v(LOG_TAG, contentN.toString());
			String text = contentN.toString();
			if (isHaveText(text)) {
				content.addText(text);
			}
			return false;
		} else if (contentN.nodeName().equals("li")) {	// list item
			content.addText("<br>");
			return true;
		} else if (contentN.nodeName().equals("br")) {	// single line break
			content.addText("<br>");
			return false;
		} else if (contentN.nodeName().equals("p")) {	// paragraph
			Element pE = (Element)contentN;
			if (pE.hasClass("imgtitle")) {
				return false;
			}
			return true;
		} else if (contentN.nodeName().equals("img")) {
			Element e = (Element)contentN;
			String src = e.attr("src");

			if (src.startsWith("images/smilies/")) {
				//emotion
				content.addText("[emoticon "+src+"]");
				return false;
			} else if (src.equals("images/common/none.gif") || src.startsWith("attachments/day_")) {
				//internal image
				content.addImg(e.attr("file"), true);
				return false;
			} else if (src.equals("images/common/")) {
				//skip common icons
				return false;
			} else if (src.startsWith("http://") || src.startsWith("https://")) {
				//external image
				content.addImg(src, false);
				return false;
			} else if (src.startsWith("images/attachicons/")) {
				//attach icon
				return false;
			} else if (src.startsWith("images/default/")) {
				//default icon
				return false;
			} else {
				//
				content.addText("[[ERROR:UNPARSED IMG:"+src+"]]");
				Log.e(LOG_TAG, "[[ERROR:UNPARSED IMG:"+src+"]]");
				return false;
			}
		} else if (contentN.nodeName().equals("span")) {	// a section in a document
			Elements attachAES = ((Element)contentN).select("a");
			Boolean isInternalAttach = false;
			for (int attIdx = 0; attIdx < attachAES.size(); attIdx++) {
				Element attachAE = attachAES.get(attIdx);
				if (attachAE.attr("href").startsWith("attachment.php?")) {
					content.addAttach(attachAE.attr("href"), attachAE.text());
					isInternalAttach = true;
				}
			}
			if (isInternalAttach) { return false; }
			return true;
		} else if (contentN.nodeName().equals("a")) {
			Element aE = (Element)contentN;
			String text = aE.text();
			String url = aE.attr("href");
			if (aE.childNodeSize() > 0 && aE.childNode(0).nodeName().equals("img")) {
				content.addLink(url, url);
				return true;
			}

			if (url.startsWith("attachment.php?")) {
				// is Attachment
				content.addAttach(url, text);
				return false;
			} 

			content.addLink(text, url);
			return false;
		} else if (contentN.nodeName().equals("div")) {	// a section in a document
			Element divE = (Element)contentN;
			if (divE.hasClass("t_attach")) {
				// remove div.t_attach
				return false;
			} else if (divE.hasClass("quote")) {
				content.addQuote(divE.text());
				return false;
			} else if (divE.hasClass("attach_popup")) {
				// remove div.attach_popup
				return false;
			}
			return true;
		} else if (contentN.nodeName().equals("table")) {
			return true;
		} else if (contentN.nodeName().equals("tbody")) {	//Groups the body content in a table
			return true;
		} else if (contentN.nodeName().equals("tr")) {	//a row in a table
			content.addText("<br>");
			return true;
		} else if (contentN.nodeName().equals("td")) {	//a cell in a table
			content.addText(" ");
			return true;
		} else if (contentN.nodeName().equals("dl")) {	//a description list
			return true;
		} else if (contentN.nodeName().equals("dt")) {	//a term/name in a description list
			return true;
		} else if (contentN.nodeName().equals("dd")) {	//a description/value of a term in a description list
			return true;
		} else if (contentN.nodeName().equals("script") || contentN.nodeName().equals("#data")) {
			// video
			String html = contentN.toString();
			String url = HttpUtils.getMiddleString(html, "'src', '", "'");
			if (url != null && url.startsWith("http://player.youku.com/player.php")) {
				//http://player.youku.com/player.php/sid/XNzIyMTUxMzEy.html/v.swf
				//http://v.youku.com/v_show/id_XNzIyMTUxMzEy.html
				url = HttpUtils.getMiddleString(url, "sid/", "/v.swf");
				url = "http://v.youku.com/v_show/id_" + url;
				if (!url.endsWith(".html")) {url = url + ".html";}
				content.addLink("YouKu视频自动转换手机通道 "+url, url);
			} else if (url != null && url.startsWith("http")) {
				content.addLink("FLASH VIDEO,手机可能不支持 "+url, url);
			}
			return false;
		} else {
			content.addText("[[ERROR:UNPARSED TAG:"+contentN.nodeName()+":"+contentN.toString()+"]]");
			Log.e(LOG_TAG, "[[ERROR:UNPARSED TAG:"+contentN.nodeName()+"]]");
			return false;
		}
	}

	private static Boolean isHaveText(String str) {
		return !str.trim().isEmpty();
	}
}
