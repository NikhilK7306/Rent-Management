package com.rentms.dto.settings;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionSettingsResponse {

    private Integer sessionTimeoutMinutes;
    private Boolean autoLogoutOnExpiration;
    private Boolean logoutOnBrowserClose;
    private Boolean invalidateSessionsOnRestart;
    private String updatedAt;
}