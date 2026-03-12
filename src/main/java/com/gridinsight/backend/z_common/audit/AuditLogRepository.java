package com.gridinsight.backend.z_common.audit;

import com.gridinsight.backend.IAM_1.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.actorUserId = :userId OR a.targetUserId = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:resource IS NULL OR a.resource = :resource) AND " +
           "(:fromDate IS NULL OR a.timestamp >= :fromDate) AND " +
           "(:toDate IS NULL OR a.timestamp <= :toDate) " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findByFilters(@Param("userId") Long userId,
                                   @Param("action") String action,
                                   @Param("resource") String resource,
                                   @Param("fromDate") Instant fromDate,
                                   @Param("toDate") Instant toDate);
}

