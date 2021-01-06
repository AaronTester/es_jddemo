package com.aaron.util;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.aaron.pojo.Goods;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author Aaron
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2021/1/5
 */
@Component
public class JsoupDataUtil {

    public List<Goods> queryGoods(String keyWords) throws IOException {
        List<Goods> list = new ArrayList<>();
        String url = "https://search.jd.com/Search?enc=utf-8&keyword="+keyWords;
        Document document = Jsoup.connect(url).get();
        Element jGoodsList = document.getElementById("J_goodsList");
        Elements li = jGoodsList.getElementsByTag("li");
        for(int i=0; i<li.size(); i++) {
            Element el = li.get(i);
            if (li.attr("class").equalsIgnoreCase("gl-item")) {
                String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
                String price = el.getElementsByClass("p-price").eq(0).text();
                String title = el.getElementsByClass("p-name").eq(0).text();
               list.add(new Goods(img, price, title));
            }
        }
        return list;
    }
}
