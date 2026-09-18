package com.rentms.dto.notification;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSummaryResponse {

    private long totalUnread;
    private long overdueCount;
    private long pendingCount;
    private long partialCount;
    private long upcomingCount;
    private long paidCount;
    private long totalOutstanding;
}