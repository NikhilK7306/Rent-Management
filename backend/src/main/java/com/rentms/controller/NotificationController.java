package com.rentms.controller;

import com.rentms.dto.notification.NotificationResponse;
import com.rentms.dto.notification.NotificationSummaryResponse;
import com.rentms.entity.User;
import com.rentms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getAllNotifications(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
    }

    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(pageable));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<Page<NotificationResponse>> getNotificationsByTenant(
            @AuthenticationPrincipal User user,
            @PathVariable Long tenantId,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotificationsByTenant(tenantId, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @GetMapping("/summary")
    public ResponseEntity<NotificationSummaryResponse> getNotificationSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getNotificationSummary());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal User user,
            @RequestBody List<Long> ids) {
        notificationService.markAsRead(ids);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}