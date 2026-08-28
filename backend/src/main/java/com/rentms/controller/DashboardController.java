package com.rentms.controller;

import com.rentms.dto.dashboard.DashboardResponse;
import com.rentms.entity.User;
import com.rentms.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final PropertyRepository propertyRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal User user) {
        long propertyCount = propertyRepository.count();
        return ResponseEntity.ok(DashboardResponse.forAdmin(user.getName(), propertyCount));
    }
}