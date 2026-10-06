package com.rentms.controller;

import com.rentms.dto.notification.NotificationResponse;
import com.rentms.dto.notification.NotificationSummaryResponse;
import com.rentms.entity.User;
import com.rentms.repository.TenantRepository;
import com.rentms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenant/notifications")
@RequiredArgsConstructor
@Slf4j
public class TenantNotificationController {

    private final NotificationService notificationService;
    private final TenantRepository tenantRepository;

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User user) {
        log.debug("GET /api/tenant/notifications/unread-count - Fetching unread count for tenant: {}", user.getMobileNumber());
        Long count = notificationService.getUnreadCountForTenant(user.getMobileNumber());
        return ResponseEntity.ok(count);
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        log.debug("GET /api/tenant/notifications - Fetching notifications for tenant: {}", user.getMobileNumber());
        Page<NotificationResponse> notifications = notificationService.getNotificationsForTenant(user.getMobileNumber(), pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@AuthenticationPrincipal User user, @PathVariable Long id) {
        log.debug("PUT /api/tenant/notifications/{}/read - Marking notification as read for tenant: {}", id, user.getMobileNumber());
        NotificationResponse response = notificationService.markAsReadForTenant(id, user.getMobileNumber());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        log.debug("PUT /api/tenant/notifications/read-all - Marking all notifications as read for tenant: {}", user.getMobileNumber());
        notificationService.markAllAsReadForTenant(user.getMobileNumber());
        return ResponseEntity.ok().build();
    }
}