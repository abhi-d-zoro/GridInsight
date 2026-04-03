package com.gridinsight.backend.a_iam.service;

import com.gridinsight.backend.a_iam.dto.AdminKpiResponse;
import com.gridinsight.backend.a_iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    // add these later when you have entities
    // private final GridZoneRepository gridZoneRepository;
    // private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public AdminKpiResponse getKpis() {

        long totalUsers = userRepository.count();

        // TEMP values until those modules are ready
        long totalGridZones = 0;
        long activeAlerts = 0;

        return new AdminKpiResponse(
                totalUsers,
                totalGridZones,
                activeAlerts
        );
    }
}