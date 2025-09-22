package com.project.news_archive.controller;

import com.project.news_archive.domain.NewsDoc;
import com.project.news_archive.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    // 전체 뉴스 조회
    @GetMapping("/all")
    public ResponseEntity<List<NewsDoc>> getAllNews() {
        try {
            List<NewsDoc> news = newsService.getAllDocs();
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 고급 검색
    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<List<NewsDoc>>> searchNews(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "publishedDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return newsService.searchNewsAsync(keyword, startDate, endDate, sortField, sortOrder, page, size)
                .thenApply(news -> ResponseEntity.ok(news))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                });
    }

    // 단건 조회
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<?>> getNewsById(@PathVariable String id) {
        return newsService.getNewsByIdAsync(id)
                .thenApply(news -> {
                    if (news != null) {
                        return ResponseEntity.ok(news);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                });
    }
}
