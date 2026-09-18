package com.rentms.controller;

import com.rentms.dto.dashboard.DashboardResponse;
import com.rentms.dto.dashboard.DashboardSummaryResponse;
import com.rentms.entity.User;
import com.rentms.repository.PropertyRepository;
import com.rentms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final PropertyRepository propertyRepository;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        long propertyCount = propertyRepository.count();
        return ResponseEntity.ok(DashboardResponse.forAdmin(user.getName(), propertyCount));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getDashboardSummary(user));
    }
}