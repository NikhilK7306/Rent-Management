package com.rentms.dto.notification;

import com.rentms.entity.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String message;
    private String priority;
    private Boolean isRead;
    private Long tenantId;
    private String tenantName;
    private Long propertyId;
    private String propertyName;
    private Long rentId;
    private Long paymentId;
    private String referenceKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .priority(notification.getPriority().name())
                .isRead(notification.getIsRead())
                .tenantId(notification.getTenant() != null ? notification.getTenant().getId() : null)
                .tenantName(notification.getTenant() != null ? notification.getTenant().getFullName() : null)
                .propertyId(notification.getProperty() != null ? notification.getProperty().getId() : null)
                .propertyName(notification.getProperty() != null ? notification.getProperty().getPropertyName() : null)
                .rentId(notification.getRent() != null ? notification.getRent().getId() : null)
                .paymentId(notification.getPayment() != null ? notification.getPayment().getId() : null)
                .referenceKey(notification.getReferenceKey())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}