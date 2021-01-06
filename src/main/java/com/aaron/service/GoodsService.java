package com.aaron.service;

import com.aaron.pojo.Goods;
import com.aaron.util.JsoupDataUtil;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author Aaron
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2021/1/5
 */
@Service
public class GoodsService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private JsoupDataUtil jsoupDataUtil;

    public boolean addGoodsIndex(String keywords) throws IOException {
        List<Goods> goods = jsoupDataUtil.queryGoods(keywords);
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("3s");
        for (Goods good : goods) {
            bulkRequest.add(
                    new IndexRequest("jd_goods")
                            .source(JSON.toJSONString(good), XContentType.JSON)
            );
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }

    public List<Map<String, Object>> queryGoods(String keywords,int pageNum,int pageSize) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest("jd_goods");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(pageNum);
        searchSourceBuilder.size(pageSize);

        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keywords);
        searchSourceBuilder.query(termQuery);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit : searchResponse.getHits().getHits()) {
            result.add(hit.getSourceAsMap());
        }
        return result;
    }

    public List<Map<String, Object>> highLightGoods(String keywords,int pageNum,int pageSize) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest("jd_goods");

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(pageNum);
        searchSourceBuilder.size(pageSize);

        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keywords);
        searchSourceBuilder.query(termQuery);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if(title != null) {
                Text[] texts = title.fragments();
                String n_title = "";
                for (Text text : texts) {
                    n_title += text;
                }
                sourceAsMap.put("title", n_title);
            }
            result.add(sourceAsMap);
        }
        return result;
    }
}
