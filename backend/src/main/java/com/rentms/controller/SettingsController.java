package com.rentms.controller;

import com.rentms.dto.settings.SessionSettingsRequest;
import com.rentms.dto.settings.SessionSettingsResponse;
import com.rentms.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/session")
    public ResponseEntity<SessionSettingsResponse> getSessionSettings() {
        return ResponseEntity.ok(settingsService.getSessionSettings());
    }

    @PutMapping("/session")
    public ResponseEntity<SessionSettingsResponse> updateSessionSettings(@Valid @RequestBody SessionSettingsRequest request) {
        SessionSettingsResponse response = settingsService.updateSessionSettings(request);
        return ResponseEntity.ok(response);
    }
}