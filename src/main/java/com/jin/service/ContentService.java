package com.jin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jin.model.Content;
import com.jin.utils.HtmlParserUtil;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    @Autowired
    @Qualifier("objectMapper")
    private ObjectMapper mapper;

    @Autowired
    private HtmlParserUtil parser;

    public boolean parseContent(String keyword) throws Exception {
        List<Content> contents = parser.parseJD(keyword);
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        for (Content content : contents) {
            String source = mapper.writeValueAsString(content);
            bulkRequest.add(new IndexRequest("jd_goods").source(source, XContentType.JSON));
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }

    public List<Map<String, Object>> searchPage(String keyword,
                                                int pageNo,
                                                int pageSize) throws IOException {
        if (pageNo <= 1) {
            pageNo = 1;
        }

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(queryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }
        return list;
    }

    public List<Map<String, Object>> searchPageHighlightBuilder(String keyword,
                                                                int pageNo,
                                                                int pageSize) throws IOException {
        if (pageNo <= 1) {
            pageNo = 1;
        }

        keyword = URLDecoder.decode(keyword, "UTF-8");

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //分页
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.query(termQueryBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(true);//多个高亮显示
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //执行搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit documentFields : search.getHits().getHits()) {

            //解析高亮的字段
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            if (title != null) {
                Text[] fragments = title.fragments();
                StringBuilder newTile = new StringBuilder();
                for (Text text : fragments) {
                    newTile.append(text);
                }
                sourceAsMap.put("title", newTile.toString());
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}
