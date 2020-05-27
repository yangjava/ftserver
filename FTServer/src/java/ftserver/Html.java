package ftserver;

import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Html {

    public static Page get(String url, HashSet<String> subUrls) {
        try {
            if (url == null || url.length() > Page.MAX_URL_LENGTH || url.length() < 8) {
                return null;
            }
            Document doc = Jsoup.connect(url).timeout(15 * 1000).get();
            if (!doc.hasText()) {
                return null;
            }

            fixSpan(doc);

            Page page = new Page();
            page.url = url;
            page.html = doc.html();
            page.text = replace(doc.body().text());

            if (page.text.length() < 10) {
                return null;
            }

            if (subUrls != null) {
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String ss = link.attr("abs:href");
                    if (ss != null && ss.length() > 8) {
                        subUrls.add(ss);
                    }
                }
            }
            return page;
        } catch (Throwable e) {
            //e.printStackTrace();
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

    public static ArrayList<PageText> getDefaultTexts(Page page) {
        if (page.id < 1) {
            //no id;
            return null;
        }

        ArrayList<PageText> result = new ArrayList<PageText>();

        Document doc = Jsoup.parse(page.html);

        String title = null;
        String keywords = null;

        String url = page.url;
        long pageId = page.id;

        try {
            title = doc.title();
        } catch (Throwable e) {

        }

        if (title == null) {
            title = "";
        }
        if (title.length() < 1) {
            title = url;
        }
        title = replace(title);
        if (title.length() > 100) {
            title = title.substring(0, 100);
        }

        keywords = getMetaContentByName(doc, "keywords");
        keywords = keywords.replaceAll(",", " ");
        if (keywords.length() > 100) {
            keywords = keywords.substring(0, 100);
        }

        PageText description = new PageText();

        description.pageId = pageId;
        description.url = url;
        description.title = title;
        description.keywords = keywords;

        description.text = getMetaContentByName(doc, "description");
        if (description.text.length() > 300) {
            description.text = description.text.substring(0, 300);
        }
        description.priorityId = PageText.descriptionPriority;
        result.add(description);

        String content = page.text;
        long startPriority = PageText.descriptionPriority - 1;
        while (startPriority > 0 && content.length() > 0) {

            PageText text = new PageText();
            description.pageId = pageId;
            description.url = url;
            description.title = title;
            description.keywords = "";

            text.text = "";

            int maxLength = PageText.max_text_length - 100;

            while (text.text.length() < maxLength && content.length() > 0) {

                String sp = " ,.　，。";
                int p1 = content.length();
                for (char c : sp.toCharArray()) {
                    int t = content.indexOf(c);
                    if (t < 1) {
                        t = content.length();
                    }
                    p1 = Math.min(p1, t);
                }

                text.text += content.substring(0, p1);

                if ((content.length() - p1) > 1) {
                    content = content.substring(p1 + 1);
                }
                if (content.length() < 100) {
                    text.text += (" " + content);
                    content = "";
                }

            }

            text.priorityId = startPriority;
            result.add(text);
            startPriority--;
        }

        return result;

    }

    private static String replace(String content) {
        return content.replaceAll(Character.toString((char) 8203), "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&gt;", " ")
                .replaceAll("&lt;", " ")
                .replaceAll("\t|\r|\n|�|<|>|�|\\$|\\|", " ")
                .replaceAll("　", " ")
                .replaceAll("\\s+", " ")
                .trim();
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
        for (Element e : doc.getElementsByTag("span")) {
            e.text(" " + e.text() + " ");
        }
        for (Element e : doc.getElementsByTag("div")) {
            e.text(" " + e.text() + " ");
        }
    }

}
