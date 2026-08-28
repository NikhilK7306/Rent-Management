package com.rentms.dto.dashboard;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private String message;
    private String adminName;
    private String role;
    private long propertyCount;
    private SystemStatus systemStatus;
    private DatabaseStatus databaseStatus;
    private BackendStatus backendStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemStatus {
        private String status = "Connected";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DatabaseStatus {
        private String status = "Connected";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BackendStatus {
        private String status = "Connected";
    }

    public static DashboardResponse forAdmin(String adminName, long propertyCount) {
        return DashboardResponse.builder()
                .message("Welcome, " + adminName)
                .adminName(adminName)
                .role("ADMIN")
                .propertyCount(propertyCount)
                .systemStatus(new SystemStatus())
                .databaseStatus(new DatabaseStatus())
                .backendStatus(new BackendStatus())
                .build();
    }
}