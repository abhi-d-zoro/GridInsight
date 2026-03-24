package com.gridinsight.backend.g_atmm.controller;

import com.gridinsight.backend.g_atmm.dto.AcknowledgeAlertDTO;
import com.gridinsight.backend.g_atmm.dto.CloseAlertDTO;
import com.gridinsight.backend.g_atmm.entity.Alert;
import com.gridinsight.backend.g_atmm.entity.AlertActivity;
import com.gridinsight.backend.g_atmm.service.AlertStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertStatusController {

    private final AlertStatusService statusService;

    @PostMapping("/{id}/acknowledge")
    public Alert acknowledge(@PathVariable Long id,
                             @Valid @RequestBody AcknowledgeAlertDTO request) {
        return statusService.acknowledge(id, request);
    }

    @PostMapping("/{id}/close")
    public Alert close(@PathVariable Long id,
                       @Valid @RequestBody CloseAlertDTO request) {
        return statusService.close(id, request);
    }

    @GetMapping("/{id}/activity")
    public List<AlertActivity> getActivity(@PathVariable Long id) {
        return statusService.getActivity(id);
    }
}