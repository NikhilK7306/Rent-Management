package com.rentms.repository;

import com.rentms.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByReferenceKey(String referenceKey);

    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();

    Page<Notification> findByIsReadFalseOrderByCreatedAtDesc(Pageable pageable);

    List<Notification> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    Page<Notification> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false")
    long countUnread();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.tenant.id = :tenantId AND n.isRead = false")
    long countUnreadByTenant(@Param("tenantId") Long tenantId);

    List<Notification> findByType(Notification.Type type);

    Page<Notification> findByType(Notification.Type type, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :type AND n.isRead = :isRead")
    long countByTypeAndIsRead(@Param("type") Notification.Type type, @Param("isRead") boolean isRead);

    List<Notification> findByPriority(Notification.Priority priority);

    Page<Notification> findByPriority(Notification.Priority priority, Pageable pageable);

    List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :ids")
    int markAsRead(@Param("ids") List<Long> ids);

    @Query("UPDATE Notification n SET n.isRead = true")
    int markAllAsRead();

    @Query("UPDATE Notification n SET n.isRead = true WHERE n.tenant.mobileNumber = :mobileNumber")
    int markAllAsReadForTenant(@Param("mobileNumber") String mobileNumber);

    @Query("SELECT n FROM Notification n WHERE n.tenant.mobileNumber = :mobileNumber ORDER BY n.createdAt DESC")
    Page<Notification> findByTenantMobileNumberOrderByCreatedAtDesc(@Param("mobileNumber") String mobileNumber, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.tenant.mobileNumber = :mobileNumber AND n.isRead = false")
    long countUnreadByTenantMobileNumber(@Param("mobileNumber") String mobileNumber);

    Optional<Notification> findByIdAndTenantMobileNumber(@Param("id") Long id, @Param("mobileNumber") String mobileNumber);
}