package com.rentms.service;

import com.rentms.dto.notification.NotificationResponse;
import com.rentms.dto.notification.NotificationSummaryResponse;
import com.rentms.entity.Notification;
import com.rentms.entity.Payment;
import com.rentms.entity.Rent;
import com.rentms.entity.Tenant;
import com.rentms.exception.NotificationNotFoundException;
import com.rentms.repository.NotificationRepository;
import com.rentms.repository.PaymentRepository;
import com.rentms.repository.RentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RentRepository rentRepository;
    private final PaymentRepository paymentRepository;

    // Reminder period in days (default 3 days before due date)
    private static final int RENT_DUE_REMINDER_DAYS = 3;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable).map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(Pageable pageable) {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc(pageable).map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByTenant(Long tenantId, Pageable pageable) {
        return notificationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable).map(NotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countUnread();
    }

    @Transactional(readOnly = true)
    public long getUnreadCountByTenant(Long tenantId) {
        return notificationRepository.countUnreadByTenant(tenantId);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        log.info("Notification marked as read: {}", id);
        return NotificationResponse.from(saved);
    }

    @Transactional
    public void markAsRead(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            notificationRepository.markAsRead(ids);
            log.info("Notifications marked as read: {}", ids);
        }
    }

    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
        log.info("All notifications marked as read");
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getNotificationSummary() {
        // Get counts for different notification types
        long overdueCount = notificationRepository.countByTypeAndIsRead(Notification.Type.RENT_OVERDUE, false);
        long pendingCount = notificationRepository.countByTypeAndIsRead(Notification.Type.RENT_PENDING, false);
        long partialCount = notificationRepository.countByTypeAndIsRead(Notification.Type.RENT_PARTIAL, false);
        long upcomingCount = notificationRepository.countByTypeAndIsRead(Notification.Type.RENT_DUE, false);
        long paidCount = notificationRepository.countByTypeAndIsRead(Notification.Type.RENT_PAID, false) +
                         notificationRepository.countByTypeAndIsRead(Notification.Type.PAYMENT_RECEIVED, false);

        // Calculate total outstanding amount from overdue/pending/partial rents
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        return NotificationSummaryResponse.builder()
                .totalUnread(notificationRepository.countUnread())
                .overdueCount(overdueCount)
                .pendingCount(pendingCount)
                .partialCount(partialCount)
                .upcomingCount(upcomingCount)
                .paidCount(paidCount)
                .totalOutstanding(totalOutstanding)
                .build();
    }

    // Duplicate prevention: check if notification already exists for this reference key
    private boolean notificationExists(String referenceKey) {
        return notificationRepository.findByReferenceKey(referenceKey).isPresent();
    }

    // Create notification with duplicate prevention
    public Notification createNotificationIfNotExists(String referenceKey, Notification.Type type,
                                                       String title, String message, Notification.Priority priority,
                                                       Tenant tenant, Property property, Rent rent, Payment payment) {
        if (notificationExists(referenceKey)) {
            log.debug("Notification already exists for reference key: {}", referenceKey);
            return null;
        }

        Notification notification = Notification.builder()
                .type(type)
                .title(title)
                .message(message)
                .priority(priority)
                .isRead(false)
                .tenant(tenant)
                .property(property)
                .rent(rent)
                .payment(payment)
                .referenceKey(referenceKey)
                .build();

        return notificationRepository.save(notification);
    }

    // Generate rent due notifications (upcoming due dates)
    @Transactional
    public void generateRentDueNotifications() {
        log.info("Generating rent due notifications");
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(RENT_DUE_REMINDER_DAYS);

        // Find rents that are due within the reminder period and are PENDING or PARTIAL
        List<Rent> upcomingRents = rentRepository.findByDueDateBetweenAndStatusIn(today, reminderDate,
                List.of(Rent.Status.PENDING, Rent.Status.PARTIAL));

        for (Rent rent : upcomingRents) {
            String referenceKey = "RENT_DUE_" + rent.getId();
            String title = "Rent Due Soon";
            String message = String.format("Rent of %s for %s is due on %s.",
                    rent.getMonthlyRent(), rent.getTenant().getFullName(), rent.getDueDate());
            Notification.Priority priority = rent.getDueDate().isEqual(today) ? Notification.Priority.HIGH : Notification.Priority.MEDIUM;

            createNotificationIfNotExists(referenceKey, Notification.Type.RENT_DUE, title, message, priority,
                    rent.getTenant(), rent.getProperty(), rent, null);
        }
    }

    // Generate pending rent notifications
    @Transactional
    public void generatePendingRentNotifications() {
        log.info("Generating pending rent notifications");
        List<Rent> pendingRents = rentRepository.findByStatus(Rent.Status.PENDING);

        for (Rent rent : pendingRents) {
            String referenceKey = "RENT_PENDING_" + rent.getId();
            String title = "Rent Pending";
            String message = String.format("Rent of %s for %s is pending.", rent.getMonthlyRent(), rent.getTenant().getFullName());

            createNotificationIfNotExists(referenceKey, Notification.Type.RENT_PENDING, title, message,
                    Notification.Priority.MEDIUM, rent.getTenant(), rent.getProperty(), rent, null);
        }
    }

    // Generate partial payment notifications
    @Transactional
    public void generatePartialPaymentNotifications() {
        log.info("Generating partial payment notifications");
        List<Rent> partialRents = rentRepository.findByStatus(Rent.Status.PARTIAL);

        for (Rent rent : partialRents) {
            String referenceKey = "RENT_PARTIAL_" + rent.getId();
            BigDecimal outstanding = rent.getMonthlyRent().subtract(rent.getPaidAmount() != null ? rent.getPaidAmount() : BigDecimal.ZERO);
            String title = "Partial Payment";
            String message = String.format("%s has partially paid the %s %d rent. %s remains outstanding.",
                    rent.getTenant().getFullName(), getMonthName(rent.getRentMonth()), rent.getRentYear(), outstanding);

            createNotificationIfNotExists(referenceKey, Notification.Type.RENT_PARTIAL, title, message,
                    Notification.Priority.MEDIUM, rent.getTenant(), rent.getProperty(), rent, null);
        }
    }

    // Generate overdue rent notifications
    @Transactional
    public void generateOverdueRentNotifications() {
        log.info("Generating overdue rent notifications");
        LocalDate today = LocalDate.now();

        // Find rents that are overdue (due date passed, not fully paid)
        List<Rent> overdueRents = rentRepository.findOverdueRents(today);

        for (Rent rent : overdueRents) {
            String referenceKey = "RENT_OVERDUE_" + rent.getId();
            BigDecimal outstanding = rent.getMonthlyRent().subtract(rent.getPaidAmount() != null ? rent.getPaidAmount() : BigDecimal.ZERO);
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(rent.getDueDate(), today);

            String title = "Rent Overdue";
            String message = String.format("%s's rent is overdue by %d days. Outstanding amount: %s.",
                    rent.getTenant().getFullName(), overdueDays, outstanding);

            Notification.Priority priority = overdueDays > 30 ? Notification.Priority.HIGH : Notification.Priority.MEDIUM;

            createNotificationIfNotExists(referenceKey, Notification.Type.RENT_OVERDUE, title, message, priority,
                    rent.getTenant(), rent.getProperty(), rent, null);
        }
    }

    // Generate aggregated outstanding rent notifications for tenants with multiple outstanding rents
    @Transactional
    public void generateAggregatedOutstandingNotifications() {
        log.info("Generating aggregated outstanding notifications");
        List<Object[]> tenantOutstandingData = rentRepository.getAggregatedOutstandingByTenant();

        for (Object[] row : tenantOutstandingData) {
            Long tenantId = ((Number) row[0]).longValue();
            String tenantName = (String) row[1];
            long pendingCount = ((Number) row[2]).longValue();
            BigDecimal totalOutstanding = (BigDecimal) row[3];

            if (pendingCount >= 3 && totalOutstanding.compareTo(BigDecimal.ZERO) > 0) {
                String referenceKey = "OUTSTANDING_AGGREGATED_" + tenantId;
                String title = "Multiple Outstanding Rents";
                String message = String.format("%s has %d pending rent records with a total outstanding amount of %s.",
                        tenantName, pendingCount, totalOutstanding);

                Tenant tenant = new Tenant();
                tenant.setId(tenantId);
                tenant.setFullName(tenantName);

                createNotificationIfNotExists(referenceKey, Notification.Type.OUTSTANDING_RENT, title, message,
                        Notification.Priority.HIGH, tenant, null, null, null);
            }
        }
    }

    // Payment received notification - called from PaymentService
    @Transactional
    public void createPaymentReceivedNotification(Payment payment) {
        Rent rent = payment.getRent();
        String referenceKey = "PAYMENT_RECEIVED_" + payment.getId();

        BigDecimal outstanding = rent.getMonthlyRent().subtract(
                rent.getPaidAmount() != null ? rent.getPaidAmount() : BigDecimal.ZERO);

        String title = "Payment Received";
        String message;
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            message = String.format("Payment of %s received from %s for %s %d rent. Rent fully paid.",
                    payment.getAmount(), rent.getTenant().getFullName(), getMonthName(rent.getRentMonth()), rent.getRentYear());
        } else {
            message = String.format("Partial payment of %s received from %s. %s remains outstanding.",
                    payment.getAmount(), rent.getTenant().getFullName(), outstanding);
        }

        createNotificationIfNotExists(referenceKey, Notification.Type.PAYMENT_RECEIVED, title, message,
                Notification.Priority.INFO, rent.getTenant(), rent.getProperty(), rent, payment);
    }

    // Fully paid notification - called when rent status changes to PAID
    @Transactional
    public void createFullyPaidNotification(Rent rent) {
        String referenceKey = "RENT_PAID_" + rent.getId();

        String title = "Rent Fully Paid";
        String message = String.format("%s %d rent for %s has been fully paid.",
                getMonthName(rent.getRentMonth()), rent.getRentYear(), rent.getTenant().getFullName());

        createNotificationIfNotExists(referenceKey, Notification.Type.RENT_PAID, title, message,
                Notification.Priority.INFO, rent.getTenant(), rent.getProperty(), rent, null);
    }

    // Scheduled job: runs daily at 8 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void scheduledNotificationGeneration() {
        log.info("Running scheduled notification generation");
        try {
            generateRentDueNotifications();
            generatePendingRentNotifications();
            generatePartialPaymentNotifications();
            generateOverdueRentNotifications();
            generateAggregatedOutstandingNotifications();
        } catch (Exception e) {
            log.error("Error during scheduled notification generation", e);
        }
    }

    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }
}