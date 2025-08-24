package com.project.news_archive.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsDoc {
    private String id;
    private String title;
    private String content;
    private String summary;
    private String url;
    private String source;
    private String source_type;
    private String published_at;
    private String lang;
}
