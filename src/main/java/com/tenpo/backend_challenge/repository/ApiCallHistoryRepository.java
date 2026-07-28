package com.tenpo.backend_challenge.repository;

import com.tenpo.backend_challenge.model.ApiCallHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiCallHistoryRepository extends JpaRepository<ApiCallHistoryEntity, Long> {
}
