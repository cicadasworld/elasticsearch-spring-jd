package com.jin.controller;

import com.jin.service.ContentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/parse/{keyword}")
    public boolean parse(@PathVariable String keyword) throws Exception {
        return contentService.parseContent(keyword);
    }

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> search(@PathVariable String keyword,
                                            @PathVariable int pageNo,
                                            @PathVariable int pageSize) throws IOException {
        //return contentService.searchPage(keyword, pageNo, pageSize);
        return contentService.searchPageHighlightBuilder(keyword, pageNo, pageSize);
    }
}