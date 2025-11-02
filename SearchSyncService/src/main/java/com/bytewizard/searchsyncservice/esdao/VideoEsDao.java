package com.bytewizard.searchsyncservice.esdao;



import com.bytewizard.searchsyncservice.domain.es.VideoEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface VideoEsDao extends ElasticsearchRepository<VideoEs, Long> {

}