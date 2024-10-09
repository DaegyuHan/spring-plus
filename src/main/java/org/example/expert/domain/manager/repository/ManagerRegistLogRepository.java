package org.example.expert.domain.manager.repository;

import org.example.expert.domain.manager.entity.ManagerRegistLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRegistLogRepository extends JpaRepository<ManagerRegistLog, Long> {
}
