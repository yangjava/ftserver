package ftserver;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import static ftserver.App.*;

public class Html {

    //static String splitWords = " ,.　，。";
    public static Page get(String url, HashSet<String> subUrls) {
        try {
            if (url == null || url.length() > Page.MAX_URL_LENGTH || url.length() < 8) {
                log("URL Length: " + url + " :" + (url != null ? url.length() : ""));
                return null;
            }
            Document doc = Jsoup.connect(url).get();
            if (!doc.hasText()) {
                return null;
            }
            if ("xml".equals(doc.outputSettings().syntax().name())) {
                log("XML " + url);
                return null;
            }
            if (doc.body() == null) {
                log("No Body " + url);
                return null;
            }

            if (subUrls != null) {
                String host = getHost(url);
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String ss = link.attr("abs:href");
                    if (ss != null && ss.length() > 8) {
                        ss = getUrl(ss);
                        String h = getHost(ss);
                        //if (host.equals(h)) 
                        {
                            subUrls.add(ss);
                        }
                    }
                }
            }
            fixSpan(doc);

            Page page = new Page();
            page.url = url;
            //page.html = doc.html();

            String text = replace(doc.body().text());

            if (text.length() < 10) {
                //some website can't get html
                log("No HTML " + url);
                return null;
            }
            if (text.length() > 100_000) {
                log("BIG HTML " + url);
                return null;
            }
            if (text.length() > 50_000) {
                log("[BigURL] " + url);
            }
            page.text = text;

            String title = null;
            String keywords = null;
            String description = null;

            try {
                title = doc.title();
            } catch (Throwable e) {

            }
            if (title == null) {
                title = "";
            }
            if (title.length() < 1) {
                //ignore no title
                log("No Title " + url);
                return null;
            }
            title = replace(title);
            if (title.length() > 300) {
                title = title.substring(0, 300);
            }

            keywords = getMetaContentByName(doc, "keywords");
            keywords = keywords.replaceAll("，", ",");

            if (keywords.length() > 200) {
                keywords = keywords.substring(0, 200);
            }

            description = getMetaContentByName(doc, "description");
            if (description.length() == 0) {
                log("Can't find description " + url);
                page.text += " " + title;
            }
            if (description.length() > 500) {
                description = description.substring(0, 500);
            }

            page.title = title;
            page.keywords = keywords;
            page.description = description;

            return page;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getMetaContentByName(Document doc, String name) {
        String description = null;
        try {
            description = doc.selectFirst("meta[name='" + name + "']").attr("content");
        } catch (Throwable e) {

        }

        try {
            if (description == null) {
                description = doc.selectFirst("meta[property='og:" + name + "']").attr("content");
            }
        } catch (Throwable e) {

        }

        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        try {
            if (description == null) {
                description = doc.selectFirst("meta[name='" + name + "']").attr("content");
            }
        } catch (Throwable e) {

        }

        if (description == null) {
            description = "";
        }

        return replace(description);
    }

    public static PageText getDefaultText(Page page, long id) {
        PageText pt = PageText.fromId(id);
        pt.url = page.url;
        pt.title = page.title;
        pt.createTime = page.createTime;
        if (pt.priority >= PageText.descriptionPriority) {
            pt.keywords = page.keywords;
        }
        if (pt.priority == PageText.userPriority) {
            pt.text = page.userDescription;
        }
        if (pt.priority == PageText.descriptionPriority || pt.priority == PageText.descriptionKeyPriority) {
            pt.text = page.description;
        }

        if (pt.priority == PageText.contextPriority) {
            pt.text = page.text;
        }
        return pt;
    }

    public static ArrayList<PageText> getDefaultTexts(Page page) {
        if (page.textOrder < 1) {
            //no id;
            return null;
        }

        ArrayList<PageText> result = new ArrayList<PageText>();

        if (page.userDescription != null && page.userDescription.length() > 0) {
            result.add(getDefaultText(page, PageText.toId(page.textOrder, PageText.userPriority)));
        }
        if (page.description != null && page.description.length() > 0) {
            long p = page.isKeyPage ? PageText.descriptionKeyPriority : PageText.descriptionPriority;
            result.add(getDefaultText(page, PageText.toId(page.textOrder, p)));
        }
        if (page.text != null && page.text.length() > 0) {
            result.add(getDefaultText(page, PageText.toId(page.textOrder, PageText.contextPriority)));
        }

        return result;
    }

    public static String replace(String content) {
        return content
                .replaceAll("　", " ")
                .replaceAll(Character.toString((char) 8203), " ")
                //.replaceAll("&nbsp;", " ")
                //.replaceAll("&gt;", " ")
                //.replaceAll("&lt;", " ")
                .replaceAll("\t|\r|\n|<|>|\\$|\\|", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String getHost(String url) {
        url = getUrl(url);
        int p = url.indexOf("//");
        if (p > 0) {
            url = url.substring(p + 2);

            p = url.indexOf("/");
            if (p > 0) {
                url = url.substring(0, p);
            }
            return url;
        }
        return url;
    }

    public static String getUrl(String name) {

        int p = name.indexOf("http://");
        if (p < 0) {
            p = name.indexOf("https://");
        }
        if (p >= 0) {
            name = name.substring(p).trim();
            int t = name.indexOf("#");
            if (t > 0) {
                name = name.substring(0, t);
            }
            t = name.indexOf(" ");
            if (t > 0) {
                name = name.substring(0, t);
            }
            return name;
        }
        return "";
    }

    private static void fixSpan(Document doc) {
        for (String s : new String[]{"script", "style", "textarea", "noscript", "code"}) {
            for (Element c : new ArrayList<Element>(doc.getElementsByTag(s))) {
                //c.parent().children().remove(c);
                c.remove();
            }
        }
        for (String s : new String[]{"span", "td", "th", "li", "a", "option", "p",
            "div", "h1", "h2", "h3", "h4", "h5", "pre"}) {
            for (Element e : doc.getElementsByTag(s)) {
                if (e.childNodeSize() == 1 && e.childNode(0) instanceof TextNode) {
                    try {
                        e.text(" " + e.text() + " ");
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

}
