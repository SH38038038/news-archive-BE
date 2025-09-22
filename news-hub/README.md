# 1) 컨테이너 기동
docker compose up -d


# 2) 인덱스 템플릿 적용 (한 번만)
curl -X PUT http://localhost:9200/_index_template/news-template \
-H 'Content-Type: application/json' \
--data-binary @es-templates/news_template.json


# 3) Python 의존성 설치
cd producer
python -m venv .venv && source .venv/bin/activate # Windows: .venv\Scripts\activate
pip install -r requirements.txt


# 4) Kafka 토픽 생성 (필요 시)
docker exec -it $(docker ps -qf name=kafka) \
kafka-topics --create --topic news_topic --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1


# 5) 프로듀서 실행 (실시간 수집)
python producer.py


# 6) Kibana 접속: http://localhost:5601 (색인 패턴: news-*)