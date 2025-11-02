package com.bytewizard.searchsyncservice.esdao;



import com.bytewizard.searchsyncservice.domain.es.UserEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserEsDao extends ElasticsearchRepository<UserEs, Long> {

}