import os
import time, hashlib, json, re
from datetime import datetime, timezone
import feedparser
from bs4 import BeautifulSoup
from kafka import KafkaProducer

# Kafka 연결
bootstrap_servers = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")

producer = KafkaProducer(
    bootstrap_servers=[bootstrap_servers],
    value_serializer=lambda v: json.dumps(v, ensure_ascii=False).encode("utf-8"),
)

# ----------------------------
# 언론사별 RSS 피드 (언론사, 카테고리, URL)
# ----------------------------
RSS_FEEDS = [
    ("한겨레", "전체", "http://www.hani.co.kr/rss/"),
    ("한겨레", "정치", "http://www.hani.co.kr/rss/politics/"),
    ("한겨레", "경제", "http://www.hani.co.kr/rss/economy/"),
    ("한겨레", "사회", "http://www.hani.co.kr/rss/society/"),
    ("한겨레", "국제", "http://www.hani.co.kr/rss/international/"),
    ("한겨레", "문화", "http://www.hani.co.kr/rss/culture/"),
    ("한겨레", "스포츠", "http://www.hani.co.kr/rss/sports/"),
    ("한겨레", "과학", "http://www.hani.co.kr/rss/science/"),
    ("한겨레", "오피니언", "http://www.hani.co.kr/rss/opinion/"),

    ("한국경제", "전체", "https://www.hankyung.com/feed/all-news"),
    ("한국경제", "정치", "https://www.hankyung.com/feed/politics"),
    ("한국경제", "경제", "https://www.hankyung.com/feed/economy"),
    ("한국경제", "사회", "https://www.hankyung.com/feed/society"),
    ("한국경제", "국제", "https://www.hankyung.com/feed/international"),
    ("한국경제", "IT", "https://www.hankyung.com/feed/it"),
    ("한국경제", "오피니언", "https://www.hankyung.com/feed/opinion"),

    ("매일경제", "경제", "https://www.mk.co.kr/rss/30100041/"),
    ("매일경제", "정치", "https://www.mk.co.kr/rss/30200030/"),
    ("매일경제", "사회", "https://www.mk.co.kr/rss/50400012/"),
    ("매일경제", "국제", "https://www.mk.co.kr/rss/30300018/"),
    ("매일경제", "문화", "https://www.mk.co.kr/rss/30000023/"),
    ("매일경제", "스포츠", "https://www.mk.co.kr/rss/71000001/"),

    ("경향신문", "정치", "https://www.khan.co.kr/rss/rssdata/politic_news.xml"),
    ("경향신문", "경제", "https://www.khan.co.kr/rss/rssdata/economy_news.xml"),
    ("경향신문", "사회", "https://www.khan.co.kr/rss/rssdata/society_news.xml"),
    ("경향신문", "국제", "https://www.khan.co.kr/rss/rssdata/kh_world.xml"),
    ("경향신문", "문화", "https://www.khan.co.kr/rss/rssdata/culture_news.xml"),
    ("경향신문", "스포츠", "http://www.khan.co.kr/rss/rssdata/kh_sports.xml"),
    ("경향신문", "과학", "https://www.khan.co.kr/rss/rssdata/science_news.xml"),
    ("경향신문", "오피니언", "https://www.khan.co.kr/rss/rssdata/opinion_news.xml"),
]

# ----------------------------
# 유틸 함수
# ----------------------------
def clean_html(html: str) -> str:
    soup = BeautifulSoup(html or "", "html5lib")
    text = soup.get_text(" ")
    return re.sub(r"\s+", " ", text).strip()

def make_id(url: str) -> str:
    return hashlib.sha256(url.encode("utf-8")).hexdigest()

# 낚시성 기사 필터링
BLOCK_KEYWORDS = ["충격", "헉", "대박", "이럴수가", "?", "과연", "ㅋㅋ", "ㅎㄷㄷ"]

def is_clickbait(title: str) -> bool:
    for kw in BLOCK_KEYWORDS:
        if kw in title:
            return True
    if re.search(r"…$", title):
        return True
    return False

# ----------------------------
# 메인 루프
# ----------------------------
while True:
    for source, category, feed_url in RSS_FEEDS:
        parsed = feedparser.parse(feed_url)
        for e in parsed.entries:
            url = getattr(e, "link", None)
            if not url:
                continue
            title = getattr(e, "title", "")

            # 🔹 낚시성 기사 제외
            if is_clickbait(title):
                continue

            summary_raw = getattr(e, "summary", "")
            summary = clean_html(summary_raw)
            published = getattr(e, "published", None) or getattr(e, "updated", None)

            doc = {
                "id": make_id(url),
                "title": title,
                "content": summary,
                "summary": summary,
                "url": url,
                "source": source,         # 언론사명
                "category": category,     # 카테고리 추가 ✅
                "source_type": "rss",
                "published_at": published or datetime.now(timezone.utc).isoformat(),
                "tags": [],
                "author": getattr(e, "author", None),
                "lang": parsed.feed.get("language", "und"),
            }

            producer.send("news_topic", {"doc": doc})
            producer.flush()

    time.sleep(60)
