package com.nova.poneglyph.repository;


import com.nova.poneglyph.domain.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

//    List<AuditLog> findByActorUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
//
//    @Query("SELECT al FROM AuditLog al WHERE " +
//            "(:actorId IS NULL OR al.actor.userId = :actorId) AND " +
//            "(:action IS NULL OR al.action LIKE %:action%) AND " +
//            "(:targetType IS NULL OR al.targetType = :targetType) AND " +
//            "(:startDate IS NULL OR al.createdAt >= :startDate) AND " +
//            "(:endDate IS NULL OR al.createdAt <= :endDate) " +
//            "ORDER BY al.createdAt DESC")
//    List<AuditLog> findByCriteria(@Param("actorId") UUID actorId,
//                                  @Param("action") String action,
//                                  @Param("targetType") String targetType,
//                                  @Param("startDate") OffsetDateTime startDate,
//                                  @Param("endDate") OffsetDateTime endDate,
//                                  Pageable pageable);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.actor.id = :userId AND " +
            "al.action LIKE 'SECURITY_%' AND al.createdAt >= :since")
    long countSecurityEvents(@Param("userId") UUID userId,
                             @Param("since") OffsetDateTime since);

    Page<AuditLog> findByActionContaining(String action, Pageable pageable);

    Page<AuditLog> findByTargetTypeAndTargetId(String targetType, String targetId, Pageable pageable);
}
