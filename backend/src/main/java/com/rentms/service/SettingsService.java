package com.rentms.service;

import com.rentms.dto.settings.SessionSettingsRequest;
import com.rentms.dto.settings.SessionSettingsResponse;
import com.rentms.entity.ApplicationSetting;
import com.rentms.repository.ApplicationSettingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {

    private final ApplicationSettingRepository settingRepository;

    private static final String SESSION_TIMEOUT_KEY = "session.timeout.minutes";
    private static final String AUTO_LOGOUT_KEY = "session.auto_logout_on_expiration";
    private static final String LOGOUT_ON_CLOSE_KEY = "session.logout_on_browser_close";
    private static final String INVALIDATE_ON_RESTART_KEY = "session.invalidate_on_restart";

    private final Map<String, String> settingsCache = new HashMap<>();

    @PostConstruct
    public void init() {
        loadAllSettings();
    }

    @Transactional(readOnly = true)
    public void loadAllSettings() {
        settingRepository.findAll().forEach(setting -> settingsCache.put(setting.getSettingKey(), setting.getSettingValue()));
        log.debug("Loaded {} settings into cache", settingsCache.size());
    }

    @Transactional(readOnly = true)
    public SessionSettingsResponse getSessionSettings() {
        return SessionSettingsResponse.builder()
                .sessionTimeoutMinutes(getIntSetting(SESSION_TIMEOUT_KEY, 30))
                .autoLogoutOnExpiration(getBooleanSetting(AUTO_LOGOUT_KEY, true))
                .logoutOnBrowserClose(getBooleanSetting(LOGOUT_ON_CLOSE_KEY, true))
                .invalidateSessionsOnRestart(getBooleanSetting(INVALIDATE_ON_RESTART_KEY, true))
                .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    @Transactional
    public SessionSettingsResponse updateSessionSettings(SessionSettingsRequest request) {
        if (request.getSessionTimeoutMinutes() != null) {
            if (request.getSessionTimeoutMinutes() < 5 || request.getSessionTimeoutMinutes() > 1440) {
                throw new IllegalArgumentException("Session timeout must be between 5 minutes and 24 hours (1440 minutes)");
            }
            updateSetting(SESSION_TIMEOUT_KEY, String.valueOf(request.getSessionTimeoutMinutes()));
        }

        if (request.getAutoLogoutOnExpiration() != null) {
            updateSetting(AUTO_LOGOUT_KEY, String.valueOf(request.getAutoLogoutOnExpiration()));
        }

        if (request.getLogoutOnBrowserClose() != null) {
            updateSetting(LOGOUT_ON_CLOSE_KEY, String.valueOf(request.getLogoutOnBrowserClose()));
        }

        if (request.getInvalidateSessionsOnRestart() != null) {
            updateSetting(INVALIDATE_ON_RESTART_KEY, String.valueOf(request.getInvalidateSessionsOnRestart()));
        }

        log.info("Session settings updated");
        return getSessionSettings();
    }

    @Transactional(readOnly = true)
    public int getSessionTimeoutMinutes() {
        return getIntSetting(SESSION_TIMEOUT_KEY, 30);
    }

    @Transactional(readOnly = true)
    public boolean isAutoLogoutOnExpiration() {
        return getBooleanSetting(AUTO_LOGOUT_KEY, true);
    }

    @Transactional(readOnly = true)
    public boolean isLogoutOnBrowserClose() {
        return getBooleanSetting(LOGOUT_ON_CLOSE_KEY, true);
    }

    @Transactional(readOnly = true)
    public boolean isInvalidateSessionsOnRestart() {
        return getBooleanSetting(INVALIDATE_ON_RESTART_KEY, true);
    }

    private int getIntSetting(String key, int defaultValue) {
        String value = settingsCache.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = settingsCache.get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private void updateSetting(String key, String value) {
        ApplicationSetting setting = settingRepository.findBySettingKey(key)
                .orElse(ApplicationSetting.builder()
                        .settingKey(key)
                        .build());
        setting.setSettingValue(value);
        setting.setDescription(setting.getDescription());
        settingRepository.save(setting);
        settingsCache.put(key, value);
    }
}