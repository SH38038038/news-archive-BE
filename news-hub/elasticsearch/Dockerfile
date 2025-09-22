FROM docker.elastic.co/elasticsearch/elasticsearch:8.13.4
RUN elasticsearch-plugin install --batch analysis-nori
USER root
RUN chown -R elasticsearch:elasticsearch /usr/share/elasticsearch
USER elasticsearch
