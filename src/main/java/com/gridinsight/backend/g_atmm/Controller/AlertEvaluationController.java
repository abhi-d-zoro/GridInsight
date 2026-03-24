package com.gridinsight.backend.g_atmm.controller;

import com.gridinsight.backend.g_atmm.dto.CheckValueRequestDTO;
import com.gridinsight.backend.g_atmm.entity.Alert;
import com.gridinsight.backend.g_atmm.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class AlertEvaluationController {

    private final AlertService alertService;

    @PostMapping("/check-value")
    public List<Alert> evaluate(@Valid @RequestBody CheckValueRequestDTO request) {
        return alertService.evaluate(request);
    }

    @GetMapping("/alerts")
    public List<Alert> getAll() {
        return alertService.getAll();
    }

    @GetMapping("/alerts/{id}")
    public Alert getById(@PathVariable Long id) {
        return alertService.getById(id);
    }
}