package com.rentms.service;

import com.rentms.dto.dashboard.DashboardSummaryResponse;
import com.rentms.dto.notification.NotificationSummaryResponse;
import com.rentms.dto.reports.*;
import com.rentms.entity.Property;
import com.rentms.entity.Rent;
import com.rentms.entity.Tenant;
import com.rentms.entity.User;
import com.rentms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RentRepository rentRepository;
    private final PaymentRepository paymentRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(User user) {
        log.info("Fetching dashboard summary for user: {}", user.getMobileNumber());

        // Property statistics
        long totalProperties = propertyRepository.count();
        long occupiedProperties = propertyRepository.countByStatusAndTenantsIsNotEmpty(Property.Status.ACTIVE);
        long vacantProperties = totalProperties - occupiedProperties;
        long activeTenants = tenantRepository.countByStatus(Tenant.Status.ACTIVE);

        // Rent summary (current month by default)
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        BigDecimal totalRentDue = rentRepository.sumMonthlyRentByFilters(null, null, currentMonth, currentYear, null, null);
        BigDecimal totalRentCollected = rentRepository.sumPaidAmountByFilters(null, null, currentMonth, currentYear, null, null);
        BigDecimal totalOutstanding = totalRentDue.subtract(totalRentCollected);

        long pendingRents = rentRepository.countByStatusAndFilters(Rent.Status.PENDING, null, null, currentMonth, currentYear, null, null) +
                rentRepository.countByStatusAndFilters(Rent.Status.OVERDUE, null, null, currentMonth, currentYear, null, null);

        // Rent summary details
        long paidCount = rentRepository.countByStatusAndFilters(Rent.Status.PAID, null, null, currentMonth, currentYear, null, null);
        long partialCount = rentRepository.countByStatusAndFilters(Rent.Status.PARTIAL, null, null, currentMonth, currentYear, null, null);

        DashboardSummaryResponse.RentSummary rentSummary = DashboardSummaryResponse.RentSummary.builder()
                .totalRent(totalRentDue)
                .paidRent(totalRentCollected)
                .partiallyPaidRent(BigDecimal.ZERO)
                .pendingRent(BigDecimal.ZERO)
                .outstandingAmount(totalOutstanding)
                .build();

        // Payment summary
        BigDecimal totalPaymentsAmount = paymentRepository.sumAmountByFilters(null, null, currentMonth, currentYear, null, null);
        long completedPayments = paymentRepository.countByStatus(com.rentms.entity.Payment.Status.PAID);
        long partialPayments = paymentRepository.countByStatus(com.rentms.entity.Payment.Status.PARTIAL);
        List<DashboardSummaryResponse.PaymentMethodBreakdown> paymentMethodBreakdown = paymentRepository.getPaymentMethodBreakdown(null, null, currentMonth, currentYear, null, null)
                .stream()
                .map(pmb -> DashboardSummaryResponse.PaymentMethodBreakdown.builder()
                        .paymentMethod(pmb.getPaymentMethod())
                        .totalAmount(pmb.getTotalAmount())
                        .count(pmb.getCount())
                        .build())
                .toList();

        DashboardSummaryResponse.PaymentSummary paymentSummary = DashboardSummaryResponse.PaymentSummary.builder()
                .totalPayments(completedPayments + partialPayments)
                .totalAmountCollected(totalPaymentsAmount)
                .completedPayments(completedPayments)
                .partialPayments(partialPayments)
                .paymentMethodBreakdown(paymentMethodBreakdown)
                .build();

        // Monthly rent overview (last 12 months)
        List<DashboardSummaryResponse.MonthlyRentOverview> monthlyOverview = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now).minusMonths(i);
            List<RentReportResponse.MonthlyRentOverview> monthData = rentRepository.getMonthlyRentOverview(ym.getYear());
            monthData.stream()
                    .filter(m -> m.getMonth() == ym.getMonthValue())
                    .findFirst()
                    .ifPresent(m -> monthlyOverview.add(DashboardSummaryResponse.MonthlyRentOverview.builder()
                            .month(m.getMonth())
                            .year(m.getYear())
                            .monthName(m.getMonthName())
                            .rentDue(m.getRentDue())
                            .collected(m.getCollected())
                            .outstanding(m.getOutstanding())
                            .paidCount(m.getPaidCount())
                            .partialCount(m.getPartialCount())
                            .pendingCount(m.getPendingCount())
                            .build()));
        }

        return DashboardSummaryResponse.builder()
                .message("Welcome, " + user.getName())
                .adminName(user.getName())
                .role(user.getRole().name())
                .totalProperties(totalProperties)
                .occupiedProperties(occupiedProperties)
                .vacantProperties(vacantProperties)
                .activeTenants(activeTenants)
                .totalRentDue(totalRentDue)
                .totalRentCollected(totalRentCollected)
                .totalOutstanding(totalOutstanding)
                .pendingRents(pendingRents)
                .rentSummary(rentSummary)
                .paymentSummary(paymentSummary)
                .monthlyRentOverview(monthlyOverview)
                .notificationSummary(notificationService.getNotificationSummary())
                .build();
    }
}