package com.jin.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.Data;

@Data
@Document(indexName = "jd_goods")
public class Content {

    @Id
    private Long id;

    private String title;
    private String img;
    private String price;
}
