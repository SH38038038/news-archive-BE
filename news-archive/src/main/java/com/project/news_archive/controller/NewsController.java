package com.project.news_archive.controller;

import com.project.news_archive.domain.NewsDoc;
import com.project.news_archive.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/api/news/all")
    public ResponseEntity<?> getAllNews() {
        try {
            List<NewsDoc> news = newsService.getAllDocs();
            return ResponseEntity.ok(news);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Elasticsearch 조회 중 오류 발생: " + e.getMessage());
        }
    }
}
