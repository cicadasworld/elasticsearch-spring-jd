package com.jin.utils;

import com.jin.model.Content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParserUtil {

    public List<Content> parseJD(String keywords) throws Exception {
        String url = "https://search.jd.com/Search?keyword=" + keywords +"&enc=utf-8";
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");

        ArrayList<Content> goodList = new ArrayList<>();
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("src");
            String price = el.select("div.p-price > strong").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            if (!img.isEmpty() && !price.isEmpty() && !title.isEmpty()) {
                Content content = new Content();
                content.setTitle(title);
                content.setPrice(price);
                content.setImg(img);
                goodList.add(content);
            }
        }
        return goodList;
    }
}
